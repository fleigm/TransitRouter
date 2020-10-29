package de.fleigm.ptmm;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.StopTime;
import com.conveyal.gtfs.model.Trip;
import org.junit.jupiter.api.Test;
import org.mapdb.Fun;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateTestFeed {

  @Test
  void asd() {
    TransitFeed transitFeed = new TransitFeed("../../files/stuttgart.zip");
    GTFSFeed feed = transitFeed.internal();

    Route route = transitFeed.busRoutes().values().stream().findFirst().get();

    Map<String, Trip> trips = transitFeed.getTripsForRoute(route);

    List<Map.Entry<Fun.Tuple2, StopTime>> stopTimes = trips.values().stream()
        .flatMap(trip -> feed.stop_times
            .subMap(Fun.t2(trip.trip_id, null), Fun.t2(trip.trip_id, Fun.HI))
            .entrySet()
            .stream())
        .collect(Collectors.toList());

    List<Map.Entry<Fun.Tuple2<String, Integer>, ShapePoint>> shapes = trips.values().stream()
        .flatMap(trip -> feed.shape_points
            .subMap(Fun.t2(trip.shape_id, -1), Fun.t2(trip.shape_id, Integer.MAX_VALUE))
            .entrySet()
            .stream())
        .collect(Collectors.toList());

    feed.routes.clear();
    feed.trips.clear();
    feed.stop_times.clear();
    feed.shape_points.clear();

    feed.routes.put(route.route_id, route);
    feed.trips.putAll(trips);
    stopTimes.forEach(stopTime -> feed.stop_times.put(stopTime.getKey(), stopTime.getValue()));
    shapes.forEach(shape -> feed.shape_points.put(shape.getKey(), shape.getValue()));

    feed.toFile("../../files/test_feed.zip");
  }
}
