package de.fleigm.transitrouter;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.routing.Observation;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A pattern combines all trips of a route that visit the same stations in the same sequence.
 * During shape generation instead of generating one shape per trip we generate only one per pattern.
 */
@Data
@Accessors(fluent = true)
public class Pattern {
  private final Route route;
  private final Type type;
  private final List<Stop> stops;
  private final List<Trip> trips;

  public Pattern(Route route, List<Stop> stops, List<Trip> trips) {
    this.route = route;
    this.stops = stops;
    this.trips = trips;
    this.type = Type.create(route.route_type);
  }

  /**
   * @return ordered stops of this pattern as observations
   */
  public List<Observation> observations() {
    return stops.stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());
  }
}
