package de.fleigm.ptmm.routing;

import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.util.TimeStep;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import java.util.List;
import java.util.Set;

import static java.lang.Math.log;

public class GraphhopperTransitionProbability implements TransitionProbability {
  public static final double DEFAULT_TRANSITION_BETA_PROBABILITY = 2.0;
  public static final int DEFAULT_U_TURN_DISTANCE_PENALTY = 1500;

  private final double transitionBetaProbability;
  private final double uTurnDistancePenalty;

  public GraphhopperTransitionProbability() {
    this(DEFAULT_TRANSITION_BETA_PROBABILITY, DEFAULT_U_TURN_DISTANCE_PENALTY);
  }

  public GraphhopperTransitionProbability(double transitionBetaProbability, double uTurnDistancePenalty) {
    this.transitionBetaProbability = transitionBetaProbability;
    this.uTurnDistancePenalty = uTurnDistancePenalty;
  }

  public static GraphhopperTransitionProbability create(PMap parameters) {
      return new GraphhopperTransitionProbability(
          parameters.getDouble("transitions_beta_probability", DEFAULT_TRANSITION_BETA_PROBABILITY),
          parameters.getDouble("u_turn_distance_penalty", DEFAULT_U_TURN_DISTANCE_PENALTY)
      );
  }

  public double getTransitionBetaProbability() {
    return transitionBetaProbability;
  }

  public double getuTurnDistancePenalty() {
    return uTurnDistancePenalty;
  }

  @Override
  public double calc(
      TimeStep<State, Observation, Path> prevTimeStep,
      TimeStep<State, Observation, Path> timeStep,
      State from,
      State to,
      Path path,
      double linearDistance,
      QueryGraph queryGraph) {

    final double penalizedPathDistance = penalizedPathDistance(path, queryGraph.getUnfavoredVirtualEdges());

    double transitionMetric = Math.abs(linearDistance - penalizedPathDistance);

    return log(1.0 / transitionBetaProbability) - (transitionMetric / transitionBetaProbability);
  }

  /**
   * Returns the path length plus a penalty if the starting/ending edge is unfavored.
   */
  private double penalizedPathDistance(
      Path path,
      Set<EdgeIteratorState> penalizedVirtualEdges) {

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


}
