package de.fleigm.ptmm.routing;

import com.graphhopper.matching.Observation;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.TransitFeed;

import java.util.List;
import java.util.stream.Collectors;

public class RoutingService {

  private TransitFeed feed;
  private TransitRouter router;

  public RoutingService(TransitFeed feed, TransitRouter router) {
    this.feed = feed;
    this.router = router;
  }

  public RoutingResult routeTrip(String tripId) {
    List<Observation> observations = feed.getOrderedStopsForTrip(tripId)
        .stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());

    return router.route(observations);
  }

}
