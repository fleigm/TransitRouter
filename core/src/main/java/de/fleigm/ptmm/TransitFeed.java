package de.fleigm.ptmm;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This wrapper class is required for injecting a gtfs feed.
 * The CDI proxy does not work with the public fields of the GTFSFeed class.
 */
public class TransitFeed {

  private final GTFSFeed feed;

  public TransitFeed(String file) {
   this(GTFSFeed.fromFile(file));
  }

  public TransitFeed(GTFSFeed gtfsFeed) {
    this.feed = gtfsFeed;
  }

  public GTFSFeed internal() {
    return feed;
  }

  public Map<String, Route> routes() {
    return feed.routes;
  }

  public Map<String, Route> busRoutes() {
    return feed.routes.entrySet().stream()
        .filter(entry -> entry.getValue().route_type == 3)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public Map<String, Trip> getTripsForRoute(Route route) {
    return feed.trips.values().stream()
        .filter(trip -> trip.route_id.equals(route.route_id))
        .collect(Collectors.toMap(trip -> trip.trip_id, trip -> trip));
  }

  public Route getRouteForTrip(String tripId) {
    return feed.routes.get(feed.trips.get(tripId).route_id);
  }

  public Map<String, Trip> trips() {
    return feed.trips;
  }

  public List<String> getOrderedStopIdsForTrip(Trip trip) {
    return feed.getOrderedStopListForTrip(trip.trip_id);
  }

  public List<Stop> getOrderedStopsForTrip(Trip trip) {
    return getOrderedStopsForTrip(trip.trip_id);
  }

  public List<Stop> getOrderedStopsForTrip(String tripId) {
    return feed.getOrderedStopListForTrip(tripId)
        .stream()
        .map(feed.stops::get)
        .collect(Collectors.toList());
  }

  public List<Pattern> findPatterns(Route route) {
    List<Trip> trips = trips().values().stream()
        .filter(trip -> trip.route_id.equals(route.route_id))
        .collect(Collectors.toList());

    return trips.stream()
        .collect(Collectors.groupingBy(this::getOrderedStopsForTrip))
        .entrySet()
        .stream()
        .map(pattern -> new Pattern(route, pattern.getKey(), pattern.getValue()))
        .collect(Collectors.toList());
  }
}
