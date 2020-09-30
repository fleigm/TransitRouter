package de.fleigm.ptmm.routing;

import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.util.TimeStep;
import com.graphhopper.routing.Path;

import java.util.List;

public class RoutingResult {

  private Path path;
  private List<TimeStep<State, Observation, Path>> timeSteps;

  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }

  public List<TimeStep<State, Observation, Path>> getTimeSteps() {
    return timeSteps;
  }

  public void setTimeSteps(List<TimeStep<State, Observation, Path>> timeSteps) {
    this.timeSteps = timeSteps;
  }
}
