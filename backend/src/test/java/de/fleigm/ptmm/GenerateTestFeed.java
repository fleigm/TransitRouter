package de.fleigm.ptmm;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.Trip;
import de.fleigm.ptmm.feeds.TransitFeed;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mapdb.Fun;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateTestFeed {

  //@Test
  void only_bus_with_one_trip_and_shape_per_pattern() {
    TransitFeed transitFeed = new TransitFeed("../../files/stuttgart.zip");
    GTFSFeed feed = transitFeed.internal();

    List<Pattern> patterns = transitFeed.busRoutes().values().stream()
        .flatMap((Route route) -> transitFeed.findPatterns(route).stream())
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

    feed.toFile("../../files/stuttgart_bus_only.zip");
  }

  @Disabled
  @Test
  void convert_distance_unit() {
    Path path = Path.of(System.getProperty("user.home"), "uni/bachelor/project/files");
    GTFSFeed feed = GTFSFeed.fromFile(path.resolve("vg.zip").toString());

    feed.shape_points.forEach((key, value) -> {
      value.shape_dist_traveled *= 1000;
      feed.shape_points.replace(key, value);
    });

    feed.stop_times.forEach((key, value) -> {
      value.shape_dist_traveled *= 1000;
      feed.stop_times.replace(key, value);
    });

    feed.toFile(path.resolve("vg_converted.zip").toString());
  }
}
