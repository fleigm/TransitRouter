package de.fleigm.transitrouter.gtfs;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import de.fleigm.transitrouter.Pattern;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Wrapper class for conveyals GTFSFeed that provides some convenient methods.
 */
public class TransitFeed {

  private final GTFSFeed feed;

  /**
   * Create TransitFeed and load zipped GTFS file.
   *
   * @param gtfsFeed path to GTFS feed
   */
  public TransitFeed(Path gtfsFeed) {
    this(gtfsFeed.toString());
  }

  /**
   * @see TransitFeed#TransitFeed(Path)
   */
  public TransitFeed(String file) {
    this(GTFSFeed.fromFile(file));
  }

  /**
   * Create a TransitFeed from a given {@link GTFSFeed}.
   *
   * @param gtfsFeed GTFS feed.
   */
  public TransitFeed(GTFSFeed gtfsFeed) {
    this.feed = gtfsFeed;
  }

  /**
   * @return original GTFSFeed
   * @see GTFSFeed
   */
  public GTFSFeed internal() {
    return feed;
  }

  /**
   * @return routes
   */
  public Map<String, Route> routes() {
    return feed.routes;
  }

  /**
   * @return all bus routes (route_type 3)
   */
  public Map<String, Route> busRoutes() {
    return feed.routes.entrySet().stream()
        .filter(entry -> entry.getValue().route_type == 3)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Get route of a given trip.
   *
   * @param tripId trip.
   * @return route.
   */
  public Route getRouteForTrip(String tripId) {
    return feed.routes.get(feed.trips.get(tripId).route_id);
  }

  /**
   * @return trips.
   */
  public Map<String, Trip> trips() {
    return feed.trips;
  }

  /**
   * @see TransitFeed#getOrderedStopsForTrip(String)
   */
  public List<Stop> getOrderedStopsForTrip(Trip trip) {
    return getOrderedStopsForTrip(trip.trip_id);
  }

  /**
   * Get the ordered stops if a given trip.
   *
   * @param tripId id.
   * @return ordered stops.
   */
  public List<Stop> getOrderedStopsForTrip(String tripId) {
    return feed.getOrderedStopListForTrip(tripId)
        .stream()
        .map(feed.stops::get)
        .collect(Collectors.toList());
  }

  /**
   * Find all patterns of a given route.
   *
   * @param route route.
   * @return patterns of given route.
   */
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
