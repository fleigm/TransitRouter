package de.fleigm.ptmm.routing;

import com.graphhopper.matching.State;
import com.graphhopper.util.DistanceCalc;

import java.util.Collection;

public class BrokenSequenceException extends RuntimeException {

  public BrokenSequenceException(String message) {
    super(message);
  }

  public static BrokenSequenceException create(int timeStepCounter,
                                               ObservationWithCandidateStates prevTimeStep,
                                               ObservationWithCandidateStates timeStep,
                                               DistanceCalc distanceCalc) {
    String likelyReasonStr = "";
    if (prevTimeStep != null) {
      double dist = distanceCalc.calcDist(
          prevTimeStep.observation.getPoint().lat,
          prevTimeStep.observation.getPoint().lon,
          timeStep.observation.getPoint().lat,
          timeStep.observation.getPoint().lon);
      if (dist > 2000) {
        likelyReasonStr = "Too long distance to previous measurement? "
                          + Math.round(dist) + "m, ";
      }
    }

    return new BrokenSequenceException(
        "Sequence is broken for submitted track at time step "
        + timeStepCounter + ". "
        + likelyReasonStr + "observation:" + timeStep.observation + ", "
        + timeStep.candidates.size() + " candidates: "
        + getSnappedCandidates(timeStep.candidates)
        + ". If a match is expected consider increasing max_visited_nodes."
    );
  }

  private static String getSnappedCandidates(Collection<State> candidates) {
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
}
