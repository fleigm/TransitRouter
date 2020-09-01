package de.fleigm.ptmm;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Agency;
import com.conveyal.gtfs.model.FeedInfo;
import com.conveyal.gtfs.model.Frequency;
import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Transfer;
import com.conveyal.gtfs.model.Trip;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mapdb.BTreeMap;
import org.mapdb.Fun;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
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

  public Map<String, Trip> trips() {
    return feed.trips;
  }

  public List<String> getOrderedStopIdsForTrip(String tripId) {
    return feed.getOrderedStopListForTrip(tripId);
  }

  public List<Stop> getOrderedStopsForTrip(String tripId) {
    return feed.getOrderedStopListForTrip(tripId)
        .stream()
        .map(feed.stops::get)
        .collect(Collectors.toList());
  }
}
