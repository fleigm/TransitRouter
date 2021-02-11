/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.fleigm.transitrouter.routing;

import com.bmw.hmm.SequenceState;
import com.bmw.hmm.ViterbiAlgorithm;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.matching.HmmProbabilities;
import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.TimeStep;
import com.graphhopper.routing.AStarBidirection;
import com.graphhopper.routing.DijkstraBidirectionRef;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.lm.LMApproximator;
import com.graphhopper.routing.lm.LandmarkStorage;
import com.graphhopper.routing.lm.PrepareLandmarks;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class matches real world GPX entries to the digital road network stored
 * in GraphHopper. The Viterbi algorithm is used to compute the most likely
 * sequence of map matching candidates. The Viterbi algorithm takes into account
 * the distance between GPX entries and map matching candidates as well as the
 * routing distances between consecutive map matching candidates.
 * <p>
 * <p>
 * See http://en.wikipedia.org/wiki/Map_matching and Newson, Paul, and John
 * Krumm. "Hidden Markov map matching through noise and sparseness." Proceedings
 * of the 17th ACM SIGSPATIAL International Conference on Advances in Geographic
 * Information Systems. ACM, 2009.
 *
 * @author Peter Karich
 * @author Michael Zilske
 * @author Stefan Holder
 * @author kodonnell
 */
public class GraphHopperTransitRouter implements TransitRouter {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  // Penalty in m for each U-turn performed at the beginning or end of a path between two
  // subsequent candidates.
  private final double uTurnDistancePenalty;

  private final Graph graph;
  private final PrepareLandmarks landmarks;
  private final LocationIndexTree locationIndex;
  private final int maxVisitedNodes;
  private final DistanceCalc distanceCalc = new DistancePlaneProjection();
  private final Weighting weighting;
  private double measurementErrorSigma = 50.0;
  private double transitionProbabilityBeta = 2.0;

  public GraphHopperTransitRouter(GraphHopper graphHopper, PMap hints) {
    this.locationIndex = (LocationIndexTree) graphHopper.getLocationIndex();

    if (hints.has("vehicle"))
      throw new IllegalArgumentException("MapMatching hints may no longer contain a vehicle, use the profile parameter instead, see core/#1958");
    if (hints.has("weighting"))
      throw new IllegalArgumentException("MapMatching hints may no longer contain a weighting, use the profile parameter instead, see core/#1958");

    if (graphHopper.getProfiles().isEmpty()) {
      throw new IllegalArgumentException("No profiles found, you need to configure at least one profile to use map matching");
    }
    if (!hints.has("profile")) {
      throw new IllegalArgumentException("You need to specify a profile to perform map matching");
    }
    String profileStr = hints.getString("profile", "");
    Profile profile = graphHopper.getProfile(profileStr);
    if (profile == null) {
      List<Profile> profiles = graphHopper.getProfiles();
      List<String> profileNames = new ArrayList<>(profiles.size());
      for (Profile p : profiles) {
        profileNames.add(p.getName());
      }
      throw new IllegalArgumentException("Could not find profile '" + profileStr + "', choose one of: " + profileNames);
    }

    // Convert heading penalty [s] into U-turn penalty [m]
    // The heading penalty is automatically taken into account by GraphHopper routing,
    // for all links that we set to "unfavored" on the QueryGraph.
    // We use that mechanism to softly enforce a heading for each map-matching state.
    // We want to consistently use the same parameter for our own objective function (independent of the routing),
    // which has meters as unit, not seconds.

    final double PENALTY_CONVERSION_VELOCITY = 5;  // [m/s]
    final double headingTimePenalty = hints.getDouble(Parameters.Routing.HEADING_PENALTY, Parameters.Routing.DEFAULT_HEADING_PENALTY);
    uTurnDistancePenalty = headingTimePenalty * PENALTY_CONVERSION_VELOCITY;

    boolean disableLM = hints.getBool(Parameters.Landmark.DISABLE, false);
    if (graphHopper.getLMPreparationHandler().isEnabled() && disableLM && !graphHopper.getRouterConfig().isLMDisablingAllowed())
      throw new IllegalArgumentException("Disabling LM is not allowed");

    boolean disableCH = hints.getBool(Parameters.CH.DISABLE, false);
    if (graphHopper.getCHPreparationHandler().isEnabled() && disableCH && !graphHopper.getRouterConfig().isCHDisablingAllowed())
      throw new IllegalArgumentException("Disabling CH is not allowed");

    // see map-matching/#177: both ch.disable and lm.disable can be used to force Dijkstra which is the better
    // (=faster) choice when the observations are close to each other
    boolean useDijkstra = disableLM || disableCH;

    if (graphHopper.getLMPreparationHandler().isEnabled() && !useDijkstra) {
      // using LM because u-turn prevention does not work properly with (node-based) CH
      List<String> lmProfileNames = new ArrayList<>();
      PrepareLandmarks lmPreparation = null;
      for (LMProfile lmProfile : graphHopper.getLMPreparationHandler().getLMProfiles()) {
        lmProfileNames.add(lmProfile.getProfile());
        if (lmProfile.getProfile().equals(profile.getName())) {
          lmPreparation = graphHopper.getLMPreparationHandler().getPreparation(
              lmProfile.usesOtherPreparation() ? lmProfile.getPreparationProfile() : lmProfile.getProfile()
          );
        }
      }
      if (lmPreparation == null) {
        throw new IllegalArgumentException("Cannot find LM preparation for the requested profile: '" + profile.getName() + "'" +
                                           "\nYou can try disabling LM using " + Parameters.Landmark.DISABLE + "=true" +
                                           "\navailable LM profiles: " + lmProfileNames);
      }
      landmarks = lmPreparation;
    } else {
      landmarks = null;
    }
    graph = graphHopper.getGraphHopperStorage();
    // since map matching does not support turn costs we have to disable them here explicitly
    weighting = graphHopper.createWeighting(profile, hints, true);
    this.maxVisitedNodes = hints.getInt(Parameters.Routing.MAX_VISITED_NODES, Integer.MAX_VALUE);

    measurementErrorSigma = hints.getDouble("measurement_error_sigma", 25);
    transitionProbabilityBeta = hints.getDouble("transitions_beta_probability", 25);
  }

  @Override
  public RoutingResult route(List<de.fleigm.transitrouter.routing.Observation> observations) {
    List<Observation> convertedObservations = observations.stream().map(o -> new Observation(o.point())).collect(Collectors.toList());

    // Snap observations to links. Generates multiple candidate snaps per observation.
    // In the next step, we will turn them into splits, but we already call them splits now
    // because they are modified in place.
    List<Collection<Snap>> splitsPerObservation = convertedObservations.stream().map(o -> locationIndex.findNClosest(o.getPoint().lat, o.getPoint().lon, DefaultEdgeFilter.allEdges(weighting.getFlagEncoder()), measurementErrorSigma))
        .collect(Collectors.toList());

    // Create the query graph, containing split edges so that all the places where an observation might have happened
    // are a node. This modifies the Snap objects and puts the new node numbers into them.
    QueryGraph queryGraph = QueryGraph.create(graph, splitsPerObservation.stream().flatMap(Collection::stream).collect(Collectors.toList()));

    // Due to how LocationIndex/QueryGraph is implemented, we can get duplicates when a point is snapped
    // directly to a tower node instead of creating a split / virtual node. No problem, but we still filter
    // out the duplicates for performance reasons.
    splitsPerObservation = splitsPerObservation.stream().map(this::deduplicate).collect(Collectors.toList());

    // Creates candidates from the Snaps of all observations (a candidate is basically a
    // Snap + direction).
    List<TimeStep<State, Observation, Path>> timeSteps = createTimeSteps(convertedObservations, splitsPerObservation, queryGraph);

    // Compute the most likely sequence of map matching candidates:
    List<SequenceState<State, Observation, Path>> seq = computeViterbiSequence(timeSteps, observations.size(), queryGraph);

    List<EdgeIteratorState> edges = seq.stream().filter(s1 -> s1.transitionDescriptor != null).flatMap(s1 -> s1.transitionDescriptor.calcEdges().stream()).collect(Collectors.toList());

    MapMatchedPath path = new MapMatchedPath(queryGraph, weighting, edges);

    return RoutingResult.builder()
        .path(path)
        .pathSegments(seq.stream().filter(x -> x.transitionDescriptor != null).map(x -> x.transitionDescriptor).collect(Collectors.toList()))
        .distance(path.getDistance())
        .time(path.getTime())
        .candidates(splitsPerObservation.stream()
            .flatMap(Collection::stream)
            .map(Snap::getSnappedPoint)
            .collect(Collectors.toList()))
        .observations(observations)
        .build();
  }

  private Collection<Snap> deduplicate(Collection<Snap> splits) {
    // Only keep one split per node number. Let's say the last one.
    Map<Integer, Snap> splitsByNodeNumber = splits.stream().collect(Collectors.toMap(Snap::getClosestNode, s -> s, (s1, s2) -> s2));
    return splitsByNodeNumber.values();
  }

  /**
   * Creates TimeSteps with candidates for the GPX entries but does not create emission or
   * transition probabilities. Creates directed candidates for virtual nodes and undirected
   * candidates for real nodes.
   */
  private List<TimeStep<State, Observation, Path>> createTimeSteps(List<Observation> filteredObservations, List<Collection<Snap>> splitsPerObservation, QueryGraph queryGraph) {
    if (splitsPerObservation.size() != filteredObservations.size()) {
      throw new IllegalArgumentException(
          "filteredGPXEntries and queriesPerEntry must have same size.");
    }

    final List<TimeStep<State, Observation, Path>> timeSteps = new ArrayList<>();
    for (int i = 0; i < filteredObservations.size(); i++) {
      Observation observation = filteredObservations.get(i);
      Collection<Snap> splits = splitsPerObservation.get(i);
      List<State> candidates = new ArrayList<>();
      for (Snap split : splits) {
        if (queryGraph.isVirtualNode(split.getClosestNode())) {
          List<VirtualEdgeIteratorState> virtualEdges = new ArrayList<>();
          EdgeIterator iter = queryGraph.createEdgeExplorer().setBaseNode(split.getClosestNode());
          while (iter.next()) {
            if (!queryGraph.isVirtualEdge(iter.getEdge())) {
              throw new RuntimeException("Virtual nodes must only have virtual edges "
                                         + "to adjacent nodes.");
            }
            virtualEdges.add((VirtualEdgeIteratorState) queryGraph.getEdgeIteratorState(iter.getEdge(), iter.getAdjNode()));
          }
          if (virtualEdges.size() != 2) {
            throw new RuntimeException("Each virtual node must have exactly 2 "
                                       + "virtual edges (reverse virtual edges are not returned by the "
                                       + "EdgeIterator");
          }

          // Create a directed candidate for each of the two possible directions through
          // the virtual node. This is needed to penalize U-turns at virtual nodes
          // (see also #51). We need to add candidates for both directions because
          // we don't know yet which is the correct one. This will be figured
          // out by the Viterbi algorithm.
          //
          // Adding further candidates to explicitly allow U-turns through setting
          // incomingVirtualEdge==outgoingVirtualEdge doesn't make sense because this
          // would actually allow to perform a U-turn without a penalty by going to and
          // from the virtual node through the other virtual edge or its reverse edge.
          candidates.add(new State(observation, split, virtualEdges.get(0), virtualEdges.get(1)));
          candidates.add(new State(observation, split, virtualEdges.get(1), virtualEdges.get(0)));
        } else {
          // Create an undirected candidate for the real node.
          candidates.add(new State(observation, split));
        }
      }

      timeSteps.add(new TimeStep<>(observation, candidates));
    }
    return timeSteps;
  }

  /**
   * Computes the most likely state sequence for the observations.
   */
  private List<SequenceState<State, Observation, Path>> computeViterbiSequence(
      List<TimeStep<State, Observation, Path>> timeSteps, int originalGpxEntriesCount,
      QueryGraph queryGraph) {
    final HmmProbabilities probabilities
        = new HmmProbabilities(measurementErrorSigma, transitionProbabilityBeta);
    final ViterbiAlgorithm<State, Observation, Path> viterbi = new ViterbiAlgorithm<>();

    logger.debug("\n=============== Paths ===============");
    int timeStepCounter = 0;
    TimeStep<State, Observation, Path> prevTimeStep = null;
    int i = 1;
    for (TimeStep<State, Observation, Path> timeStep : timeSteps) {
      logger.debug("\nPaths to time step {}", i++);
      computeEmissionProbabilities(timeStep, probabilities);

      if (prevTimeStep == null) {
        viterbi.startWithInitialObservation(timeStep.observation, timeStep.candidates,
            timeStep.emissionLogProbabilities);
      } else {
        computeTransitionProbabilities(prevTimeStep, timeStep, probabilities, queryGraph);
        viterbi.nextStep(timeStep.observation, timeStep.candidates,
            timeStep.emissionLogProbabilities, timeStep.transitionLogProbabilities,
            timeStep.roadPaths);
      }
      if (viterbi.isBroken()) {
        String likelyReasonStr = "";
        if (prevTimeStep != null) {
          double dist = distanceCalc.calcDist(prevTimeStep.observation.getPoint().lat, prevTimeStep.observation.getPoint().lon, timeStep.observation.getPoint().lat, timeStep.observation.getPoint().lon);
          if (dist > 2000) {
            likelyReasonStr = "Too long distance to previous measurement? "
                              + Math.round(dist) + "m, ";
          }
        }

        throw new IllegalArgumentException("Sequence is broken for submitted track at time step "
                                           + timeStepCounter + " (" + originalGpxEntriesCount + " points). "
                                           + likelyReasonStr + "observation:" + timeStep.observation + ", "
                                           + timeStep.candidates.size() + " candidates: "
                                           + getSnappedCandidates(timeStep.candidates)
                                           + ". If a match is expected consider increasing max_visited_nodes.");
      }

      timeStepCounter++;
      prevTimeStep = timeStep;
    }

    return viterbi.computeMostLikelySequence();
  }

  private void computeEmissionProbabilities(TimeStep<State, Observation, Path> timeStep,
                                            HmmProbabilities probabilities) {
    for (State candidate : timeStep.candidates) {
      // road distance difference in meters
      final double distance = candidate.getSnap().getQueryDistance();
      timeStep.addEmissionLogProbability(candidate,
          probabilities.emissionLogProbability(distance));
    }
  }

  private void computeTransitionProbabilities(TimeStep<State, Observation, Path> prevTimeStep,
                                              TimeStep<State, Observation, Path> timeStep,
                                              HmmProbabilities probabilities,
                                              QueryGraph queryGraph) {
    final double linearDistance = distanceCalc.calcDist(prevTimeStep.observation.getPoint().lat,
        prevTimeStep.observation.getPoint().lon, timeStep.observation.getPoint().lat, timeStep.observation.getPoint().lon);

    for (State from : prevTimeStep.candidates) {
      for (State to : timeStep.candidates) {
        // enforce heading if required:
        if (from.isOnDirectedEdge()) {
          // Make sure that the path starting at the "from" candidate goes through
          // the outgoing edge.
          queryGraph.unfavorVirtualEdge(from.getIncomingVirtualEdge().getEdge());
        }
        if (to.isOnDirectedEdge()) {
          // Make sure that the path ending at "to" candidate goes through
          // the incoming edge.
          queryGraph.unfavorVirtualEdge(to.getOutgoingVirtualEdge().getEdge());
        }

        RoutingAlgorithm router;
        if (landmarks != null) {
          AStarBidirection algo = new AStarBidirection(queryGraph, weighting, TraversalMode.NODE_BASED) {
            @Override
            protected void initCollections(int size) {
              super.initCollections(50);
            }
          };
          LandmarkStorage lms = landmarks.getLandmarkStorage();
          int activeLM = Math.min(8, lms.getLandmarkCount());
          algo.setApproximation(LMApproximator.forLandmarks(queryGraph, lms, activeLM));
          router = algo;
        } else {
          router = new DijkstraBidirectionRef(queryGraph, weighting, TraversalMode.NODE_BASED) {
            @Override
            protected void initCollections(int size) {
              super.initCollections(50);
            }
          };
        }
        router.setMaxVisitedNodes(maxVisitedNodes);

        final Path path = router.calcPath(from.getSnap().getClosestNode(),
            to.getSnap().getClosestNode());

        if (path.isFound()) {
          timeStep.addRoadPath(from, to, path);

          // The router considers unfavored virtual edges using edge penalties
          // but this is not reflected in the path distance. Hence, we need to adjust the
          // path distance accordingly.
          final double penalizedPathDistance = penalizedPathDistance(path,
              queryGraph.getUnfavoredVirtualEdges());

          logger.debug("Path from: {}, to: {}, penalized path length: {}",
              from, to, penalizedPathDistance);

          final double transitionLogProbability = probabilities
              .transitionLogProbability(penalizedPathDistance, linearDistance);
          timeStep.addTransitionLogProbability(from, to, transitionLogProbability);
        } else {
          logger.debug("No path found for from: {}, to: {}", from, to);
        }
        queryGraph.clearUnfavoredStatus();

      }
    }
  }

  /**
   * Returns the path length plus a penalty if the starting/ending edge is unfavored.
   */
  private double penalizedPathDistance(Path path, Set<EdgeIteratorState> penalizedVirtualEdges) {
    double totalPenalty = 0;

    // Unfavored edges in the middle of the path should not be penalized because we are
    // only concerned about the direction at the start/end.
    final List<EdgeIteratorState> edges = path.calcEdges();
    if (!edges.isEmpty()) {
      if (penalizedVirtualEdges.contains(edges.get(0))) {
        totalPenalty += uTurnDistancePenalty;
      }
    }
    if (edges.size() > 1) {
      if (penalizedVirtualEdges.contains(edges.get(edges.size() - 1))) {
        totalPenalty += uTurnDistancePenalty;
      }
    }
    return path.getDistance() + totalPenalty;
  }

  private String getSnappedCandidates(Collection<State> candidates) {
    String str = "";
    for (State gpxe : candidates) {
      if (!str.isEmpty()) {
        str += ", ";
      }
      str += "distance: " + gpxe.getSnap().getQueryDistance() + " to "
             + gpxe.getSnap().getSnappedPoint();
    }
    return "[" + str + "]";
  }

  private static class MapMatchedPath extends Path {
    MapMatchedPath(Graph graph, Weighting weighting, List<EdgeIteratorState> edges) {
      super(graph);
      int prevEdge = EdgeIterator.NO_EDGE;
      for (EdgeIteratorState edge : edges) {
        addDistance(edge.getDistance());
        addTime(GHUtility.calcMillisWithTurnMillis(weighting, edge, false, prevEdge));
        addEdge(edge.getEdge());
        prevEdge = edge.getEdge();
      }
      if (edges.isEmpty()) {
        setFound(false);
      } else {
        setFromNode(edges.get(0).getBaseNode());
        setFound(true);
      }
    }
  }

}