package de.fleigm.transitrouter;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.StopTime;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.util.DistanceCalcEarth;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.Type;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mapdb.Fun;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class GenerateTestFeed {

  @Disabled
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

  @Disabled
  @Test
  void compute_average_stop_distance() {
    TransitFeed transitFeed = new TransitFeed("../../files/vg.zip");
    GTFSFeed feed = transitFeed.internal();

    DoubleSummaryStatistics statistics = transitFeed.routes().values().stream()
        .filter(route -> Type.create(route.route_type) == Type.BUS)
        .map(transitFeed::findPatterns)
        .flatMap(Collection::stream)
        .map(Pattern::stops)
        .flatMapToDouble(this::distances)
        .summaryStatistics();

    System.out.println(statistics.toString());
  }

  private DoubleStream distances(List<Stop> stops) {
    DistanceCalcEarth distanceCalc = new DistanceCalcEarth();
    List<Double> distances = new ArrayList<>();
    for (int i = 1; i < stops.size(); i++) {
      Stop a = stops.get(i - 1);
      Stop b = stops.get(i);
      distances.add(distanceCalc.calcDist(a.stop_lat, a.stop_lon, b.stop_lat, b.stop_lon));
    }

    return distances.stream().mapToDouble(value -> value);
  }

  @Disabled
  @Test
  void difference_of_stations_and_path_segments() throws IOException {
    DistanceCalcEarth distanceCalc = new DistanceCalcEarth();
    TransitFeed transitFeed = new TransitFeed("../../files/stuttgart.zip");
    GTFSFeed feed = transitFeed.internal();

    List<Iterable<StopTime>> result = transitFeed.routes().values().stream()
        .filter(route -> route.route_type == 3)
        .map(transitFeed::findPatterns)
        .flatMap(Collection::stream)
        .map(pattern -> pattern.trips().get(0))
        .map(trip -> transitFeed.internal().getOrderedStopTimesForTrip(trip.trip_id))
        .collect(Collectors.toList());

    List<Double> distances = new ArrayList<>();

    for (Iterable<StopTime> stopTimes : result) {
      Iterator<StopTime> iterator = stopTimes.iterator();
      StopTime prev = iterator.next();
      while (iterator.hasNext()) {
        StopTime current = iterator.next();
        Stop prevStop = feed.stops.get(prev.stop_id);
        Stop currentStop = feed.stops.get(current.stop_id);

        double dist = Math.abs(current.shape_dist_traveled - prev.shape_dist_traveled - distanceCalc.calcDist(prevStop.stop_lat, prevStop.stop_lon, currentStop.stop_lat, currentStop.stop_lon));
        if (Double.isFinite(dist)) {
          distances.add(dist);
        }
        prev = current;
        //System.out.println(dist);
      }
      //System.out.println("---");
    }

    DoubleSummaryStatistics statistics = distances.stream()
        .mapToDouble(value -> value)
        .summaryStatistics();

    System.out.println(statistics);

    Files.write(Path.of("../../files/stuttgart_bus_distances.txt"), distances.stream().map(Object::toString).collect(Collectors.toList()));
  }
}
