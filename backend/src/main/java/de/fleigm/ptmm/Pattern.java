package de.fleigm.ptmm;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.routing.Observation;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Accessors(fluent = true)
public class Pattern {
  private final Route route;
  private final List<Stop> stops;
  private final List<Trip> trips;

  public List<Observation> observations() {
    return stops.stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());
  }
}
