package de.fleigm.ptmm.routing;

import com.graphhopper.matching.MatchResult;
import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.util.TimeStep;
import com.graphhopper.routing.Path;

import java.util.List;

public class RoutingResult {

  private final MatchResult matchResult;
  private final List<TimeStep<State, Observation, Path>> timeSteps;

  public RoutingResult(MatchResult matchResult, List<TimeStep<State, Observation, Path>> timeSteps) {
    this.matchResult = matchResult;
    this.timeSteps = timeSteps;
  }

  public MatchResult getMatchResult() {
    return matchResult;
  }

  public List<TimeStep<State, Observation, Path>> getTimeSteps() {
    return timeSteps;
  }
}
