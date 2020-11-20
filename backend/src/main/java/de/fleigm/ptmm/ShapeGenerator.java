package de.fleigm.ptmm;

import com.conveyal.gtfs.model.Stop;
import com.graphhopper.matching.Observation;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.TransitRouter;

import java.util.List;
import java.util.stream.Collectors;

public class ShapeGenerator {

  private final TransitRouter transitRouter;

  public ShapeGenerator(TransitRouter transitRouter) {
    this.transitRouter = transitRouter;
  }

  public Shape generate(Pattern pattern) {
    List<Observation> observations = getObservations(pattern.stops());

    return generate(observations);
  }

  public Shape generate(List<Observation> observations) {
    RoutingResult route = transitRouter.route(observations);

    return new Shape(route.getPath().calcPoints());
  }

  private List<Observation> getObservations(List<Stop> stops) {
    return stops.stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());
  }

}
