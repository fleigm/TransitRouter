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
package de.fleigm.ptmm.routing;

import com.bmw.hmm.SequenceState;
import com.bmw.hmm.Transition;
import com.bmw.hmm.ViterbiAlgorithm;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.matching.HmmProbabilities;
import com.graphhopper.matching.Observation;
import com.graphhopper.routing.AStarBidirection;
import com.graphhopper.routing.BidirRoutingAlgorithm;
import com.graphhopper.routing.DijkstraBidirectionRef;
import com.graphhopper.routing.Path;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class TransitRouter {
  private static final String DEFAULT_PROFILE = "bus_custom_shortest";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Graph graph;
  private final PrepareLandmarks landmarks;
  private final LocationIndexTree locationIndex;
  private final int maxVisitedNodes;
  private final DistanceCalc distanceCalc = new DistancePlaneProjection();
  private final Weighting weighting;

  private final double candidateSearchRadius;
  private final double sigma;
  private final double beta;

  public TransitRouter(GraphHopper graphHopper, PMap hints) {
    this.locationIndex = (LocationIndexTree) graphHopper.getLocationIndex();

    Profile profile = graphHopper.getProfile(hints.getString("profile", DEFAULT_PROFILE));

    boolean disableLM = hints.getBool(Parameters.Landmark.DISABLE, false);
    if (graphHopper.getLMPreparationHandler().isEnabled() && disableLM && !graphHopper.getRouterConfig().isLMDisablingAllowed())
      throw new IllegalArgumentException("Disabling LM is not allowed");

    boolean disableCH = hints.getBool(Parameters.CH.DISABLE, false);
    if (graphHopper.getCHPreparationHandler().isEnabled() && disableCH && !graphHopper.getRouterConfig().isCHDisablingAllowed())
      throw new IllegalArgumentException("Disabling CH is not allowed");

    // see map-matching/#177: both ch.disable and lm.disable can be used to force Dijkstra which is the better
    // (=faster) choice when the observations are close to each other
    boolean useDijkstra = disableLM || disableCH;

    landmarks = prepareLandmarks(graphHopper, profile, useDijkstra);
    graph = graphHopper.getGraphHopperStorage();
    weighting = graphHopper.createWeighting(profile, hints);
    maxVisitedNodes = hints.getInt(Parameters.Routing.MAX_VISITED_NODES, Integer.MAX_VALUE);

    candidateSearchRadius = hints.getDouble("candidate_search_radius", 25);
    sigma = hints.getDouble("measurement_error_sigma", 25);
    beta = hints.getDouble("transitions_beta_probability", 25);
  }

  private PrepareLandmarks prepareLandmarks(GraphHopper graphHopper, Profile profile, boolean useDijkstra) {
    final PrepareLandmarks landmarks;
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
    return landmarks;
  }

  public RoutingResult route(List<Observation> observations) {
    // Snap observations to links. Generates multiple candidate snaps per observation.
    // In the next step, we will turn them into splits, but we already call them splits now
    // because they are modified in place.
    List<Collection<Snap>> splitsPerObservation = observations.stream()
        .map(o -> locationIndex.findNClosest(
            o.getPoint().lat,
            o.getPoint().lon,
            DefaultEdgeFilter.allEdges(weighting.getFlagEncoder()), candidateSearchRadius))
        .collect(Collectors.toList());

    // Create the query graph, containing split edges so that all the places where an observation might have happened
    // are a node. This modifies the Snap objects and puts the new node numbers into them.
    QueryGraph queryGraph = QueryGraph.create(
        graph,
        splitsPerObservation.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));

    // Due to how LocationIndex/QueryGraph is implemented, we can get duplicates when a point is snapped
    // directly to a tower node instead of creating a split / virtual node. No problem, but we still filter
    // out the duplicates for performance reasons.
    splitsPerObservation = splitsPerObservation.stream()
        .map(this::deduplicate)
        .collect(Collectors.toList());

    // Creates candidates from the Snaps of all observations (a candidate is basically a
    // Snap + direction).
    List<ObservationWithCandidates> timeSteps = createTimeSteps(observations, splitsPerObservation, queryGraph);

    // Compute the most likely sequence of map matching candidates:
    List<SequenceState<DirectedCandidate, Observation, Path>> seq = computeViterbiSequence(timeSteps, queryGraph);

    List<EdgeIteratorState> path = seq.stream()
        .filter(s1 -> s1.transitionDescriptor != null)
        .flatMap(s1 -> s1.transitionDescriptor.calcEdges().stream())
        .collect(Collectors.toList());

    return RoutingResult.builder()
        .path(new MapMatchedPath(queryGraph, weighting, path))
        .distance(seq.stream().filter(s -> s.transitionDescriptor != null).mapToDouble(s -> s.transitionDescriptor.getDistance()).sum())
        .time(seq.stream().filter(s -> s.transitionDescriptor != null).mapToLong(s -> s.transitionDescriptor.getTime()).sum())
        .candidates(splitsPerObservation.stream().flatMap(Collection::stream).map(Snap::getSnappedPoint).collect(Collectors.toList()))
        .observations(observations)
        .build();
  }

  private Collection<Snap> deduplicate(Collection<Snap> splits) {
    // Only keep one split per node number. Let's say the last one.
    return splits.stream()
        .collect(Collectors.toMap(Snap::getClosestNode, s -> s, (s1, s2) -> s2))
        .values();
  }

  /**
   * Creates TimeSteps with candidates for the GPX entries but does not create emission or
   * transition probabilities. Creates directed candidates for virtual nodes and undirected
   * candidates for real nodes.
   */
  private List<ObservationWithCandidates> createTimeSteps(List<Observation> observations,
                                                          List<Collection<Snap>> splitsPerObservation,
                                                          QueryGraph queryGraph) {

    if (splitsPerObservation.size() != observations.size()) {
      throw new IllegalArgumentException(
          "filteredGPXEntries and queriesPerEntry must have same size.");
    }

    final List<ObservationWithCandidates> timeSteps = new ArrayList<>();
    for (int i = 0; i < observations.size(); i++) {
      Observation observation = observations.get(i);
      Collection<Snap> splits = splitsPerObservation.get(i);
      List<DirectedCandidate> candidates = new ArrayList<>();
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
          // the virtual node. We need to add candidates for both directions because
          // we don't know yet which is the correct one. This will be figured
          // out by the Viterbi algorithm.
          //candidates.add(new State(observation, split, virtualEdges.get(0), virtualEdges.get(1)));
          //candidates.add(new State(observation, split, virtualEdges.get(1), virtualEdges.get(0)));

          candidates.add(DirectedCandidate.builder()
              .observation(observation)
              .snap(split)
              .incomingEdge(virtualEdges.get(0))
              .outgoingEdge(virtualEdges.get(1))
              .build());

          candidates.add(DirectedCandidate.builder()
              .observation(observation)
              .snap(split)
              .incomingEdge(virtualEdges.get(1))
              .outgoingEdge(virtualEdges.get(0))
              .build());
        } else {
          // Create an undirected candidate for the real node.
          //candidates.add(new State(observation, split));

          EdgeIterator edgeIterator = queryGraph.createEdgeExplorer().setBaseNode(split.getClosestNode());
          while (edgeIterator.next()) {
            EdgeIteratorState edge = queryGraph.getEdgeIteratorState(edgeIterator.getEdge(), edgeIterator.getAdjNode());
            DirectedCandidate directedCandidate = DirectedCandidate.builder()
                .observation(observation)
                .snap(split)
                .incomingEdge(null)
                .outgoingEdge(edge)
                .build();

            candidates.add(directedCandidate);
          }
        }
      }

      timeSteps.add(new ObservationWithCandidates(observation, candidates));
    }
    return timeSteps;
  }

  /**
   * Computes the most likely state sequence for the observations.
   */
  private List<SequenceState<DirectedCandidate, Observation, Path>> computeViterbiSequence(
      List<ObservationWithCandidates> timeSteps,
      QueryGraph queryGraph) {

    final HmmProbabilities probabilities = new HmmProbabilities(sigma, beta);
    final ViterbiAlgorithm<DirectedCandidate, Observation, Path> viterbi = new ViterbiAlgorithm<>(true);

    int timeStepCounter = 0;
    ObservationWithCandidates prevTimeStep = null;
    for (ObservationWithCandidates timeStep : timeSteps) {
      final Map<DirectedCandidate, Double> emissionLogProbabilities = new HashMap<>();
      final Map<Transition<DirectedCandidate>, Double> transitionLogProbabilities = new HashMap<>();
      final Map<Transition<DirectedCandidate>, Path> roadPaths = new HashMap<>();

      // compute emission probabilities
      for (DirectedCandidate candidate : timeStep.candidates()) {
        // distance from observation to road in meters
        final double distance = candidate.snap().getQueryDistance();
        emissionLogProbabilities.put(candidate, probabilities.emissionLogProbability(distance));
      }

      if (prevTimeStep == null) {
        viterbi.startWithInitialObservation(timeStep.observation(), timeStep.candidates(), emissionLogProbabilities);
      } else {
        final double linearDistance = distanceCalc.calcDist(
            prevTimeStep.observation().getPoint().lat,
            prevTimeStep.observation().getPoint().lon,
            timeStep.observation().getPoint().lat,
            timeStep.observation().getPoint().lon);

        for (DirectedCandidate from : prevTimeStep.candidates()) {
          for (DirectedCandidate to : timeStep.candidates()) {

            final Path path = createRouter(queryGraph).calcPath(
                from.snap().getClosestNode(),
                to.outgoingEdge().getAdjNode(),
                from.outgoingEdge().getEdge(),
                to.outgoingEdge().getEdge());

            if (path.isFound()) {
              path.getEdges().remove(path.getEdgeCount() - 1);

              double transitionLogProbability = probabilities.transitionLogProbability(path.getDistance(), linearDistance);
              Transition<DirectedCandidate> transition = new Transition<>(from, to);
              roadPaths.put(transition, path);
              transitionLogProbabilities.put(transition, transitionLogProbability);
            }
          }
        }

        viterbi.nextStep(
            timeStep.observation(),
            timeStep.candidates(),
            emissionLogProbabilities,
            transitionLogProbabilities,
            roadPaths);
      }
      if (viterbi.isBroken()) {
        throw BrokenSequenceException.create(timeStepCounter, prevTimeStep, timeStep, distanceCalc);
      }

      timeStepCounter++;
      prevTimeStep = timeStep;
    }

    return viterbi.computeMostLikelySequence();
  }

  private BidirRoutingAlgorithm createRouter(QueryGraph queryGraph) {
    BidirRoutingAlgorithm router;
    if (landmarks != null) {
      AStarBidirection algo = new AStarBidirection(queryGraph, weighting, TraversalMode.EDGE_BASED) {
        @Override
        protected void initCollections(int size) {
          super.initCollections(50);
        }
      };
      LandmarkStorage lms = landmarks.getLandmarkStorage();
      int activeLM = Math.min(8, lms.getLandmarkCount());
      algo.setApproximation(LMApproximator.forLandmarks(queryGraph, lms, activeLM));
      algo.setMaxVisitedNodes(maxVisitedNodes);
      router = algo;
    } else {
      router = new DijkstraBidirectionRef(queryGraph, weighting, TraversalMode.EDGE_BASED) {
        @Override
        protected void initCollections(int size) {
          super.initCollections(50);
        }
      };
      router.setMaxVisitedNodes(maxVisitedNodes);
    }
    return router;
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