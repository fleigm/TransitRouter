package de.fleigm.ptmm.routing;

import com.graphhopper.util.DistanceCalc;

import java.util.Collection;

/**
 * This exception is thrown if no path could be found between
 * any two candidates of two observations.
 */
public class BrokenSequenceException extends RuntimeException {

  public BrokenSequenceException(String message) {
    super(message);
  }

  /**
   * Create BrokenSequenceException.
   *
   * @param timeStepCounter at which step we could not find any path
   * @param prevTimeStep    previous time step
   * @param timeStep        current time step
   * @param distanceCalc    distance calc
   * @return BrokenSequenceException
   */
  public static BrokenSequenceException create(int timeStepCounter,
                                               ObservationWithCandidates prevTimeStep,
                                               ObservationWithCandidates timeStep,
                                               DistanceCalc distanceCalc) {
    String likelyReasonStr = "";
    if (prevTimeStep != null) {
      double dist = distanceCalc.calcDist(
          prevTimeStep.observation().lat(),
          prevTimeStep.observation().lon(),
          timeStep.observation().lat(),
          timeStep.observation().lon());
      if (dist > 2000) {
        likelyReasonStr = "Too long distance to previous measurement? "
                          + Math.round(dist) + "m, ";
      }
    }

    return new BrokenSequenceException(
        "Sequence is broken for submitted track at time step "
        + timeStepCounter + ". "
        + likelyReasonStr + "observation:" + timeStep.observation() + ", "
        + timeStep.candidates().size() + " candidates: "
        + getSnappedCandidates(timeStep.candidates())
        + ". If a match is expected consider increasing max_visited_nodes."
    );
  }

  private static String getSnappedCandidates(Collection<DirectedCandidate> candidates) {
    String str = "";
    for (DirectedCandidate gpxe : candidates) {
      if (!str.isEmpty()) {
        str += ", ";
      }
      str += "distance: " + gpxe.snap().getQueryDistance() + " to "
             + gpxe.snap().getSnappedPoint();
    }
    return "[" + str + "]";
  }
}
