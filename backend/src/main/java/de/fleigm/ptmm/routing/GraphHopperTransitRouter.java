package de.fleigm.ptmm.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.util.PMap;

import java.util.List;
import java.util.stream.Collectors;

public class GraphHopperTransitRouter implements TransitRouter {

  private final MapMatching mapMatching;

  public GraphHopperTransitRouter(GraphHopper graphHopper, PMap hints) {
    mapMatching = new MapMatching(graphHopper, hints);

    mapMatching.setMeasurementErrorSigma(hints.getDouble("measurement_error_sigma", 25));
    mapMatching.setTransitionProbabilityBeta(hints.getDouble("transitions_beta_probability", 2.0));
  }

  @Override
  public RoutingResult route(List<Observation> observations) {

    MatchResult result = mapMatching.match(observations.stream()
        .map(observation -> new com.graphhopper.matching.Observation(observation.point()))
        .collect(Collectors.toList()));

    return RoutingResult.builder()
        .observations(observations)
        .path(result.getMergedPath())
        .build();
  }
}
