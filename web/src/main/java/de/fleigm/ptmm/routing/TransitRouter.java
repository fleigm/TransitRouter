package de.fleigm.ptmm.routing;

import com.bmw.hmm.SequenceState;
import com.bmw.hmm.ViterbiAlgorithm;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.util.TimeStep;
import com.graphhopper.routing.DijkstraBidirectionCH;
import com.graphhopper.routing.DijkstraBidirectionRef;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.RoutingAlgorithmFactory;
import com.graphhopper.routing.ch.CHRoutingAlgorithmFactory;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.RoutingCHGraphImpl;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.QueryResult;
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

public class TransitRouter {
  private static final String DEFAULT_PROFILE = "bus_shortest";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Graph routingGraph;
  private final LocationIndexTree locationIndex;
  private final int maxVisitedNodes;
  private final DistanceCalc distanceCalc = new DistancePlaneProjection();
  private final Weighting weighting;
  private final boolean ch;

  private final double candidateSearchRadius;
  private final List<EmissionProbability> emissionProbabilityFunctions;
  private final List<TransitionProbability> transitionProbabilityFunctions;

  public TransitRouter(GraphHopper graphHopper, PMap hints) {
    locationIndex = (LocationIndexTree) graphHopper.getLocationIndex();

    Profile profile = graphHopper.getProfile(hints.getString("profile", DEFAULT_PROFILE));

    boolean disableCH = hints.getBool(Parameters.CH.DISABLE, false);
    boolean disableLM = hints.getBool(Parameters.Landmark.DISABLE, false);
    RoutingAlgorithmFactory routingAlgorithmFactory = graphHopper.getAlgorithmFactory(profile.getName(), disableCH, disableLM);
    if (routingAlgorithmFactory instanceof CHRoutingAlgorithmFactory) {
      ch = true;
      routingGraph = graphHopper.getGraphHopperStorage().getCHGraph(((CHRoutingAlgorithmFactory) routingAlgorithmFactory).getCHConfig());
    } else {
      ch = false;
      routingGraph = graphHopper.getGraphHopperStorage();
    }
    // since map matching does not support turn costs we have to disable them here explicitly
    weighting = graphHopper.createWeighting(profile, hints, false);
    maxVisitedNodes = hints.getInt(Parameters.Routing.MAX_VISITED_NODES, Integer.MAX_VALUE);

    candidateSearchRadius = hints.getDouble("candidate_search_radius", 25);
    emissionProbabilityFunctions = List.of(GraphhopperEmissionProbability.create(hints));
    transitionProbabilityFunctions = List.of(GraphhopperTransitionProbability.create(hints));
  }

  /**
   * This method does the actual map matching.
   * <p>
   *
   * @param observations the input list with GPX points which should match to edges
   *                     of the graph specified in the constructor
   */
  public RoutingResult route(List<Observation> observations) {
    List<Collection<QueryResult>> queriesPerEntry = findCandidates(observations);

    QueryGraph queryGraph = buildQueryGraph(queriesPerEntry);

    // Different QueryResults can have the same tower node as their closest node.
    // Hence, we now dedupe the query results of each GPX entry by their closest node (#91).
    // This must be done after calling queryGraph.create() since this replaces some of the
    // QueryResult nodes with virtual nodes. Virtual nodes are not deduped since there is at
    // most one QueryResult per edge and virtual nodes are inserted into the middle of an edge.
    // Reducing the number of QueryResults improves performance since less shortest/fastest
    // routes need to be computed.
    queriesPerEntry = deduplicateQueryResultsByClosestNode(queriesPerEntry);

    List<TimeStep<State, Observation, Path>> timeSteps = createTimeSteps(
        observations,
        queriesPerEntry,
        queryGraph);

    List<SequenceState<State, Observation, Path>> viterbiSequence = computeViterbiSequence(timeSteps, queryGraph);

    return computeRoutingResult(viterbiSequence, timeSteps, queryGraph);
  }

  /**
   * Creates a QueryGraph containing the candidates that are represented by a virtual node.
   *
   * @param queriesPerEntry candidates that will be added to the QueryGraph
   * @return a QueryGraph
   */
  private QueryGraph buildQueryGraph(List<Collection<QueryResult>> queriesPerEntry) {
    // Add virtual nodes and edges to the graph so that candidates on edges can be represented
    // by virtual nodes.
    List<QueryResult> allQueryResults = new ArrayList<>();
    for (Collection<QueryResult> qrs : queriesPerEntry) {
      allQueryResults.addAll(qrs);
    }
    return QueryGraph.create(routingGraph, allQueryResults);
  }

  /**
   * Find the possible locations (edges) of each Observation in the graph.
   */
  private List<Collection<QueryResult>> findCandidates(List<Observation> observations) {

    final List<Collection<QueryResult>> gpxEntryLocations = new ArrayList<>();
    for (Observation observation : observations) {
      final List<QueryResult> queryResults = locationIndex.findNClosest(
          observation.getPoint().lat,
          observation.getPoint().lon,
          DefaultEdgeFilter.allEdges(weighting.getFlagEncoder()),
          candidateSearchRadius);
      gpxEntryLocations.add(queryResults);
    }
    return gpxEntryLocations;
  }

  private List<Collection<QueryResult>> deduplicateQueryResultsByClosestNode(
      List<Collection<QueryResult>> queriesPerEntry) {

    final List<Collection<QueryResult>> result = new ArrayList<>(queriesPerEntry.size());

    for (Collection<QueryResult> queryResults : queriesPerEntry) {
      final Map<Integer, QueryResult> dedupedQueryResults = new HashMap<>();
      for (QueryResult qr : queryResults) {
        dedupedQueryResults.put(qr.getClosestNode(), qr);
      }
      result.add(dedupedQueryResults.values());
    }
    return result;
  }

  /**
   * Creates TimeSteps with candidates for the GPX entries but does not create emission or
   * transition probabilities. Creates directed candidates for virtual nodes and undirected
   * candidates for real nodes.
   */
  private List<TimeStep<State, Observation, Path>> createTimeSteps(
      List<Observation> filteredGPXEntries,
      List<Collection<QueryResult>> queriesPerEntry,
      QueryGraph queryGraph) {

    final int n = filteredGPXEntries.size();
    if (queriesPerEntry.size() != n) {
      throw new IllegalArgumentException(
          "filteredGPXEntries and queriesPerEntry must have same size.");
    }

    final List<TimeStep<State, Observation, Path>> timeSteps = new ArrayList<>();
    for (int i = 0; i < n; i++) {

      Observation gpxEntry = filteredGPXEntries.get(i);
      final Collection<QueryResult> queryResults = queriesPerEntry.get(i);

      List<State> candidates = new ArrayList<>();
      for (QueryResult qr : queryResults) {
        int closestNode = qr.getClosestNode();
        if (queryGraph.isVirtualNode(closestNode)) {
          // get virtual edges:
          List<VirtualEdgeIteratorState> virtualEdges = new ArrayList<>();
          EdgeIterator iter = queryGraph.createEdgeExplorer().setBaseNode(closestNode);
          while (iter.next()) {
            virtualEdges.add((VirtualEdgeIteratorState)
                queryGraph.getEdgeIteratorState(iter.getEdge(), iter.getAdjNode()));
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
          VirtualEdgeIteratorState e1 = virtualEdges.get(0);
          VirtualEdgeIteratorState e2 = virtualEdges.get(1);
          for (int j = 0; j < 2; j++) {
            // get favored/unfavored edges:
            VirtualEdgeIteratorState incomingVirtualEdge = j == 0 ? e1 : e2;
            VirtualEdgeIteratorState outgoingVirtualEdge = j == 0 ? e2 : e1;
            // create candidate
            QueryResult vqr = new QueryResult(qr.getQueryPoint().lat, qr.getQueryPoint().lon);
            vqr.setQueryDistance(qr.getQueryDistance());
            vqr.setClosestNode(qr.getClosestNode());
            vqr.setWayIndex(qr.getWayIndex());
            vqr.setSnappedPosition(qr.getSnappedPosition());
            vqr.setClosestEdge(qr.getClosestEdge());
            vqr.calcSnappedPoint(distanceCalc);
            State candidate = new State(gpxEntry, vqr, incomingVirtualEdge,
                outgoingVirtualEdge);
            candidates.add(candidate);
          }
        } else {
          // Create an undirected candidate for the real node.
          State candidate = new State(gpxEntry, qr);
          candidates.add(candidate);
        }
      }

      final TimeStep<State, Observation, Path> timeStep = new TimeStep<>(gpxEntry, candidates);
      timeSteps.add(timeStep);
    }
    return timeSteps;
  }

  /**
   * Computes the most likely candidate sequence for the GPX entries.
   */
  private List<SequenceState<State, Observation, Path>> computeViterbiSequence(
      List<TimeStep<State, Observation, Path>> timeSteps,
      QueryGraph queryGraph) {

    final ViterbiAlgorithm<State, Observation, Path> viterbi = new ViterbiAlgorithm<>();

    TimeStep<State, Observation, Path> prevTimeStep = null;
    for (TimeStep<State, Observation, Path> timeStep : timeSteps) {
      computeEmissionProbabilities(timeStep, queryGraph);

      if (prevTimeStep == null) {
        viterbi.startWithInitialObservation(
            timeStep.observation,
            timeStep.candidates,
            timeStep.emissionLogProbabilities);
      } else {
        computeTransitionProbabilities(prevTimeStep, timeStep, queryGraph);
        viterbi.nextStep(
            timeStep.observation,
            timeStep.candidates,
            timeStep.emissionLogProbabilities,
            timeStep.transitionLogProbabilities,
            timeStep.roadPaths);
      }

      if (viterbi.isBroken()) {
        throw new IllegalArgumentException("Sequence is broken for submitted track");
      }

      prevTimeStep = timeStep;
    }

    return viterbi.computeMostLikelySequence();
  }

  private void computeEmissionProbabilities(
      TimeStep<State, Observation, Path> timeStep,
      QueryGraph queryGraph) {

    for (State candidate : timeStep.candidates) {
      double emissionProbability = 0;
      for (EmissionProbability f : emissionProbabilityFunctions) {
        emissionProbability += f.calc(timeStep, candidate, queryGraph);
      }

      timeStep.addEmissionLogProbability(candidate, emissionProbability);
    }
  }

  private void computeTransitionProbabilities(
      TimeStep<State, Observation, Path> prevTimeStep,
      TimeStep<State, Observation, Path> timeStep,
      QueryGraph queryGraph) {

    final double linearDistance = distanceCalc.calcDist(
        prevTimeStep.observation.getPoint().lat,
        prevTimeStep.observation.getPoint().lon,
        timeStep.observation.getPoint().lat,
        timeStep.observation.getPoint().lon);

    for (State from : prevTimeStep.candidates) {
      for (State to : timeStep.candidates) {
        final Path path = computePathBetweenCandidates(from, to, queryGraph);

        if (path.isFound()) {
          timeStep.addRoadPath(from, to, path);

          double transitionLogProbability = 0;
          for (TransitionProbability f : transitionProbabilityFunctions) {
            transitionLogProbability += f.calc(prevTimeStep, timeStep, from, to, path, linearDistance, queryGraph);
          }

          timeStep.addTransitionLogProbability(from, to, transitionLogProbability);
        } else {
          logger.debug("No path found for from: {}, to: {}", from, to);
        }
        queryGraph.clearUnfavoredStatus();

      }
    }
  }

  private Path computePathBetweenCandidates(State from, State to, QueryGraph queryGraph) {
    // enforce heading if required:
    if (from.isOnDirectedEdge()) {
      // Make sure that the path starting at the "from" candidate goes through
      // the outgoing edge.
      queryGraph.unfavorVirtualEdgePair(
          from.getQueryResult().getClosestNode(),
          from.getIncomingVirtualEdge().getEdge());
    }
    if (to.isOnDirectedEdge()) {
      // Make sure that the path ending at "to" candidate goes through
      // the incoming edge.
      queryGraph.unfavorVirtualEdgePair(
          to.getQueryResult().getClosestNode(),
          to.getOutgoingVirtualEdge().getEdge());
    }

    RoutingAlgorithm router = createRoutingAlgorithm(queryGraph);

    return router.calcPath(from.getQueryResult().getClosestNode(), to.getQueryResult().getClosestNode());
  }

  private RoutingAlgorithm createRoutingAlgorithm(QueryGraph queryGraph) {
    return ch ? createCHRoutingAlgorithm(queryGraph) : createDijkstraRoutingAlgorithm(queryGraph);
  }

  private RoutingAlgorithm createCHRoutingAlgorithm(QueryGraph queryGraph) {
    RoutingAlgorithm router = new DijkstraBidirectionCH(new RoutingCHGraphImpl(queryGraph, weighting)) {
      @Override
      protected void initCollections(int size) {
        super.initCollections(50);
      }
    };
    router.setMaxVisitedNodes(maxVisitedNodes);
    return router;
  }

  private RoutingAlgorithm createDijkstraRoutingAlgorithm(QueryGraph queryGraph) {
    RoutingAlgorithm router = new DijkstraBidirectionRef(queryGraph, weighting, TraversalMode.EDGE_BASED) {
      @Override
      protected void initCollections(int size) {
        super.initCollections(50);
      }
    };
    router.setMaxVisitedNodes(maxVisitedNodes);
    return router;
  }

  private RoutingResult computeRoutingResult(
      List<SequenceState<State, Observation, Path>> seq,
      List<TimeStep<State, Observation, Path>> timeSteps,
      QueryGraph queryGraph) {

    double distance = 0.0;
    long time = 0;
    for (SequenceState<State, Observation, Path> transitionAndState : seq) {
      if (transitionAndState.transitionDescriptor != null) {
        distance += transitionAndState.transitionDescriptor.getDistance();
        time += transitionAndState.transitionDescriptor.getTime();
      }
    }

    List<EdgeIteratorState> edges = new ArrayList<>();
    for (SequenceState<State, Observation, Path> state : seq) {
      if (state.transitionDescriptor != null) {
        edges.addAll(state.transitionDescriptor.calcEdges());
      }
    }
    Path mergedPath = new TransitRouter.MapMatchedPath(queryGraph.getBaseGraph(), weighting, edges);

    RoutingResult routingResult = new RoutingResult();
    routingResult.setPath(mergedPath);
    routingResult.setDistance(distance);
    routingResult.setTime(time);
    routingResult.setTimeSteps(timeSteps);

    return routingResult;
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
