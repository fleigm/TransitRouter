package de.fleigm.ptmm;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.matching.Observation;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.TransitRouter;

import java.util.List;
import java.util.stream.Collectors;

public class ShapeGenerator {

  private final TransitFeed transitFeed;
  private final TransitRouter transitRouter;

  public ShapeGenerator(TransitFeed transitFeed, TransitRouter transitRouter) {
    this.transitFeed = transitFeed;
    this.transitRouter = transitRouter;
  }

  public List<Shape> generate(Route route) {
    return transitFeed.findPatterns(route)
        .stream()
        .map(this::generate)
        .collect(Collectors.toList());
  }

  public Shape generate(Pattern pattern) {
    List<Observation> observations = getObservations(pattern.stops());

    return generate(observations);
  }

  public Shape generate(Trip trip) {
    List<Observation> observations = getObservations(transitFeed.getOrderedStopsForTrip(trip));

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
