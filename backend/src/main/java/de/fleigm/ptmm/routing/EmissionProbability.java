package de.fleigm.ptmm.routing;

import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.TimeStep;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.querygraph.QueryGraph;

@FunctionalInterface
public interface EmissionProbability {

  double calc(TimeStep<State, Observation, Path> timeStep, State candidate, QueryGraph queryGraph);
}
