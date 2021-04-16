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
import com.graphhopper.config.Profile;
import com.graphhopper.matching.HmmProbabilities;
import com.graphhopper.routing.BidirRoutingAlgorithm;
import com.graphhopper.routing.DijkstraBidirectionRef;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the TRMM approach with support for turn restrictions
 * and inter hop turn restrictions.
 * <p>
 * Configuration parameters:
 * - profile
 * - disable_turn_costs
 * - candidate_search_radius
 * - measurement_error_sigma
 * - transitions_beta_probability
 */
public class TransitRouterMapMatching implements TransitRouter {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Graph graph;
  private final LocationIndexTree locationIndex;
  private final DistanceCalc distanceCalc = new DistancePlaneProjection();
  private final Weighting weighting;

  private final double candidateSearchRadius;
  private final HmmProbabilities probabilities;

  public TransitRouterMapMatching(GraphHopper graphHopper, PMap hints) {
    this.locationIndex = (LocationIndexTree) graphHopper.getLocationIndex();

    Profile profile = graphHopper.getProfile(hints.getString("profile", ""));

    graph = graphHopper.getGraphHopperStorage();
    weighting = graphHopper.createWeighting(
        profile, hints, hints.getBool("disable_turn_costs", false));

    candidateSearchRadius = hints.getDouble("candidate_search_radius", 10);
    double sigma = hints.getDouble("measurement_error_sigma", 10);
    double beta = hints.getDouble("transitions_beta_probability", 1);
    probabilities = new HmmProbabilities(sigma, beta);
  }

  @Override
  public RoutingResult route(List<Observation> observations) {
    List<Collection<Snap>> snapsPerObservation = observations.stream()
        .map(this::findObservationSnaps)
        .collect(Collectors.toList());

    QueryGraph queryGraph = QueryGraph.create(
        graph,
        snapsPerObservation.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));

    List<TimeStep> timeSteps = createTimeSteps(observations, snapsPerObservation, queryGraph);

    var sequence = computeViterbiSequence(timeSteps, queryGraph);

    Path path = buildPathFromSequence(sequence, queryGraph, weighting);
    List<Path> pathSegments = sequence.stream()
        .filter(x -> x.transitionDescriptor != null)
        .map(x -> x.transitionDescriptor)
        .collect(Collectors.toList());

    return RoutingResult.builder()
        .path(path)
        .pathSegments(pathSegments)
        .distance(path.getDistance())
        .time(path.getTime())
        .candidates(snapsPerObservation.stream()
            .flatMap(Collection::stream)
            .map(Snap::getSnappedPoint)
            .collect(Collectors.toList()))
        .observations(observations)
        .build();
  }

  private List<Snap> findObservationSnaps(Observation o) {
    return locationIndex.findNClosest(
        o.lat(),
        o.lon(),
        DefaultEdgeFilter.allEdges(weighting.getFlagEncoder()), candidateSearchRadius);
  }

  /**
   * Creates a {@link TimeStep} with directed candidates for each observations
   */
  private List<TimeStep> createTimeSteps(
      List<Observation> observations,
      List<Collection<Snap>> snapsPerObservation,
      QueryGraph queryGraph) {

    List<TimeStep> timeSteps = new ArrayList<>();
    for (int i = 0; i < observations.size(); i++) {
      Observation observation = observations.get(i);
      Collection<Snap> snaps = snapsPerObservation.get(i);
      List<DirectedCandidate> candidates = createCandidates(queryGraph, observation, snaps);

      timeSteps.add(new TimeStep(observation, candidates));
    }
    return timeSteps;
  }

  private List<DirectedCandidate> createCandidates(QueryGraph queryGraph,
                                                   Observation observation,
                                                   Collection<Snap> snaps) {

    List<DirectedCandidate> candidates = new ArrayList<>();
    for (Snap split : snaps) {
      EdgeIterator edgeIterator = queryGraph
          .createEdgeExplorer()
          .setBaseNode(split.getClosestNode());

      while (edgeIterator.next()) {
        EdgeIteratorState edge = queryGraph.getEdgeIteratorState(
            edgeIterator.getEdge(),
            edgeIterator.getAdjNode());

        DirectedCandidate directedCandidate = DirectedCandidate.builder()
            .observation(observation)
            .snap(split)
            .outgoingEdge(edge)
            .build();

        candidates.add(directedCandidate);
      }
    }
    return candidates;
  }

  /**
   * Computes the most likely state sequence for the observations.
   */
  private List<SequenceState<DirectedCandidate, Observation, Path>> computeViterbiSequence(
      List<TimeStep> timeSteps,
      QueryGraph queryGraph) {

    ViterbiAlgorithm<DirectedCandidate, Observation, Path> viterbi = new ViterbiAlgorithm<>();

    int timeStepCounter = 0;
    TimeStep prevTimeStep = null;
    for (TimeStep timeStep : timeSteps) {
      HMMStep hmmStep = new HMMStep();

      computeEmissionProbabilities(timeStep, hmmStep);

      if (prevTimeStep == null) {
        viterbi.startWithInitialObservation(
            timeStep.observation(),
            timeStep.candidates(),
            hmmStep.emissionProbabilities());
      } else {
        computeTransitionProbabilities(queryGraph, prevTimeStep, timeStep, hmmStep);

        viterbi.nextStep(
            timeStep.observation(),
            timeStep.candidates(),
            hmmStep.emissionProbabilities(),
            hmmStep.transitionProbabilities(),
            hmmStep.roadPaths());
      }
      if (viterbi.isBroken()) {
        throw BrokenSequenceException.create(
            timeStepCounter,
            prevTimeStep,
            timeStep,
            distanceCalc);
      }

      timeStepCounter++;
      prevTimeStep = timeStep;
    }

    return viterbi.computeMostLikelySequence();
  }

  private void computeTransitionProbabilities(QueryGraph queryGraph,
                                              TimeStep prevTimeStep,
                                              TimeStep timeStep,
                                              HMMStep hmmStep) {

    final double linearDistance = distanceCalc.calcDist(
        prevTimeStep.observation().lat(),
        prevTimeStep.observation().lon(),
        timeStep.observation().lat(),
        timeStep.observation().lon());

    for (DirectedCandidate from : prevTimeStep.candidates()) {
      for (DirectedCandidate to : timeStep.candidates()) {

        final Path path = createRouter(queryGraph).calcPath(
            from.snap().getClosestNode(),
            to.outgoingEdge().getAdjNode(),
            from.outgoingEdge().getEdge(),
            to.outgoingEdge().getEdge());

        if (path.isFound()) {
          path.setDistance(path.getDistance() - to.outgoingEdge().getDistance());

          int last = path.getEdges().get(path.getEdgeCount() - 1);
          int secondLast = path.getEdges().get(path.getEdgeCount() - 2);
          if (last == secondLast) {
            //path.setDistance(1.2 * path.getDistance());
            path.setDistance(path.getDistance() + to.outgoingEdge().getDistance());
          }

          path.getEdges().remove(path.getEdgeCount() - 1);
          path.setEndNode(to.outgoingEdge().getBaseNode());

          double probability = probabilities.transitionLogProbability(path.getDistance(), linearDistance);
          hmmStep.addTransition(from, to, path, probability);
        }
      }
    }
  }

  private void computeEmissionProbabilities(TimeStep timeStep, HMMStep hmmStep) {
    for (DirectedCandidate candidate : timeStep.candidates()) {
      // distance from observation to road in meters
      double distance = candidate.snap().getQueryDistance();
      hmmStep.addEmissionProbability(candidate, probabilities.emissionLogProbability(distance));
    }
  }

  private BidirRoutingAlgorithm createRouter(QueryGraph queryGraph) {
    return new DijkstraBidirectionRef(queryGraph, weighting, TraversalMode.EDGE_BASED) {
      @Override
      protected void initCollections(int size) {
        super.initCollections(50);
      }
    };
  }

  private Path buildPathFromSequence(
      List<SequenceState<DirectedCandidate, Observation, Path>> sequence,
      Graph graph,
      Weighting weighting) {

    List<EdgeIteratorState> traveledEdges = sequence.stream()
        .filter(s1 -> s1.transitionDescriptor != null)
        .flatMap(s1 -> s1.transitionDescriptor.calcEdges().stream())
        .collect(Collectors.toList());

    return buildPathFromTraveledEdges(traveledEdges, graph, weighting);
  }

  private Path buildPathFromTraveledEdges(List<EdgeIteratorState> traveledEdges,
                                          Graph graph,
                                          Weighting weighting) {
    Path path = new Path(graph);

    int prevEdge = EdgeIterator.NO_EDGE;
    for (EdgeIteratorState edge : traveledEdges) {
      path.addDistance(edge.getDistance());
      path.addTime(GHUtility.calcMillisWithTurnMillis(weighting, edge, false, prevEdge));
      path.addEdge(edge.getEdge());
      prevEdge = edge.getEdge();
    }
    if (traveledEdges.isEmpty()) {
      path.setFound(false);
    } else {
      path.setFromNode(traveledEdges.get(0).getBaseNode());
      path.setFound(true);
    }

    return path;
  }
}