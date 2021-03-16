package de.fleigm.transitrouter;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.Trip;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import org.junit.jupiter.api.Test;
import org.mapdb.Fun;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateTestFeed {

  @Test
  void only_one_trip_and_shape_per_pattern() {
    TransitFeed transitFeed = new TransitFeed("../../files/stuttgart.zip");
    GTFSFeed feed = transitFeed.internal();

    List<Pattern> patterns = transitFeed.routes().values().stream()
        .flatMap((Route route) -> transitFeed.findPatterns(route).stream().sorted(Comparator.comparingInt(pattern -> pattern.stops().size())).limit(5))
        .collect(Collectors.toList());

    List<Trip> trips = patterns.stream()
        .map(pattern -> pattern.trips().get(0))
        .collect(Collectors.toList());

    var stopTimes = trips.stream()
        .flatMap(trip -> feed.stop_times
            .subMap(Fun.t2(trip.trip_id, null), Fun.t2(trip.trip_id, Fun.HI))
            .entrySet()
            .stream())
        .collect(Collectors.toList());

    List<Map.Entry<Fun.Tuple2<String, Integer>, ShapePoint>> shapes = trips.stream()
        .flatMap(trip -> feed.shape_points
            .subMap(Fun.t2(trip.shape_id, -1), Fun.t2(trip.shape_id, Integer.MAX_VALUE))
            .entrySet()
            .stream())
        .collect(Collectors.toList());

    feed.routes.clear();
    feed.trips.clear();
    feed.stop_times.clear();
    feed.shape_points.clear();

    patterns.forEach(pattern -> feed.routes.put(pattern.route().route_id, pattern.route()));
    trips.forEach(trip -> feed.trips.put(trip.trip_id, trip));
    stopTimes.forEach(stopTime -> feed.stop_times.put(stopTime.getKey(), stopTime.getValue()));
    shapes.forEach(shape -> feed.shape_points.put(shape.getKey(), shape.getValue()));

    feed.toFile("../../files/stuttgart_min.zip");
  }
}
