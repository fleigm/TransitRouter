package de.fleigm.ptmm.routing;

import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.util.HmmProbabilities;
import com.graphhopper.matching.util.TimeStep;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.querygraph.QueryGraph;

@FunctionalInterface
public interface TransitionProbability {

  double calc(
      TimeStep<State, Observation, Path> prevTimeStep,
      TimeStep<State, Observation, Path> timeStep,
      State from,
      State to,
      Path path,
      double linearDistance,
      QueryGraph queryGraph);
}
