package de.fleigm.transitrouter.gtfs;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Agency;
import com.conveyal.gtfs.model.FeedInfo;
import de.fleigm.transitrouter.data.Extension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains details of a GTFS feed.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedDetails implements Extension {
  private FeedInfo info;
  private List<Agency> agencies;
  private int routes;
  private int trips;
  private Map<Integer, Long> routesPerType;
  private Map<Integer, Long> tripsPerType;


  /**
   * Extract feed details from a given TransitFeed / GTFS feed.
   *
   * @param transitFeed transit feed.
   * @return feed details
   */
  public static FeedDetails extract(TransitFeed transitFeed) {
    GTFSFeed feed = transitFeed.internal();

    Map<Integer, Long> routesPerType = transitFeed.routes().values().stream()
        .collect(Collectors.groupingBy(route -> route.route_type, Collectors.counting()));

    Map<Integer, Long> tripsPerType = transitFeed.trips().values().stream()
        .collect(Collectors.groupingBy(trip ->
            feed.routes.get(trip.route_id).route_type, Collectors.counting()));

    return FeedDetails.builder()
        .info(feed.getFeedInfo())
        .agencies(new ArrayList<>(feed.agency.values()))
        .routes(feed.routes.size())
        .trips(feed.trips.size())
        .routesPerType(routesPerType)
        .tripsPerType(tripsPerType)
        .build();
  }
}
