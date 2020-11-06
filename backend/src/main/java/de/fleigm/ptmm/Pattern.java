package de.fleigm.ptmm;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;

import java.util.List;

public class Pattern {
  private final Route route;
  private final List<Stop> stops;
  private final List<Trip> trips;

  public Pattern(Route route, List<Stop> stops, List<Trip> trips) {
    this.route = route;
    this.stops = stops;
    this.trips = trips;
  }

  public Route route() {
    return route;
  }

  public List<Stop> stops() {
    return stops;
  }

  public List<Trip> trips() {
    return trips;
  }
}
