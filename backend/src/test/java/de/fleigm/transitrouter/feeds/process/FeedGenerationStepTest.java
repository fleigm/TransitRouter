package de.fleigm.transitrouter.feeds.process;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import de.fleigm.transitrouter.Pattern;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedGenerationStepTest {


  @Test
  void routed_pattern() {
    TransitFeed feed = new TransitFeed(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    Trip trip = feed.trips().values().stream().findFirst().get();
    Route route = feed.routes().get(trip.route_id);
    List<Stop> stops = feed.getOrderedStopsForTrip(trip);

    Pattern pattern = new Pattern(route, stops, List.of(trip));

    FeedGenerationStep.RoutedPattern routedPattern = FeedGenerationStep.RoutedPattern.of(pattern);

    assertEquals(stops.size(), routedPattern.getDistances().length);
  }

}