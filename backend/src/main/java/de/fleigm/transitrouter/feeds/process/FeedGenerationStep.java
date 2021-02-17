package de.fleigm.transitrouter.feeds.process;

import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.StopTime;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.routing.Path;
import de.fleigm.transitrouter.Pattern;
import de.fleigm.transitrouter.Shape;
import de.fleigm.transitrouter.feeds.Error;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.api.TransitRouterFactory;
import de.fleigm.transitrouter.gtfs.Feed;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.routing.Observation;
import de.fleigm.transitrouter.routing.RoutingResult;
import de.fleigm.transitrouter.routing.TransitRouter;
import de.fleigm.transitrouter.util.Helper;
import de.fleigm.transitrouter.util.StopWatch;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.Fun;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

@Slf4j
public class FeedGenerationStep implements Step {

  private final TransitRouterFactory transitRouterFactory;

  public FeedGenerationStep(TransitRouterFactory transitRouterFactory) {
    this.transitRouterFactory = transitRouterFactory;
  }

  @Override
  public void run(GeneratedFeed info) {
    TransitFeed transitFeed = new TransitFeed(info.getOriginalFeed().getPath());

    new Runner(info, transitFeed).run();
  }

  private class Runner {
    private final GeneratedFeed info;
    private final TransitFeed transitFeed;
    private final Map<Type, TransitRouter> routers = new HashMap<>();

    public Runner(GeneratedFeed info, TransitFeed transitFeed) {
      this.info = info;
      this.transitFeed = transitFeed;

      info.getParameters().forEach((type, parameters) ->
          routers.put(type, transitRouterFactory.create(parameters)));
    }

    void run() {
      log.info("Start feed generation step.");

      StopWatch stopWatch = StopWatch.createAndStart();

      transitFeed.routes()
          .values()
          .parallelStream()
          .filter(route -> info.getParameters().containsKey(Type.create(route.route_type)))
          .flatMap(route -> transitFeed.findPatterns(route).stream())
          .map(this::generateShape)
          .forEach(this::store);

      info.setFeed(
          Feed.createFromTransitFeed(
              info.getFileStoragePath().resolve(GeneratedFeed.GENERATED_GTFS_FEED),
              transitFeed));

      stopWatch.stop();

      info.getOrCreateExtension(ExecutionTime.class, ExecutionTime::new)
          .add("feed_generation", Duration.of(stopWatch.getNanos(), ChronoUnit.NANOS));

      log.info("Finished feed generation step. Took {}s", stopWatch.getSeconds());
    }

    private RoutedPattern generateShape(Pattern pattern) {
      List<Observation> observations = pattern.observations();

      try {
        return RoutedPattern.of(pattern, routers.get(pattern.type()).route(observations));
      } catch (Exception exception) {
        info.addError(
            Error.of(
                "shape_generation_failed",
                "shape generation failed, use straight line shape.",
                exception
            )
                .addDetail("route", pattern.route())
                .addDetail("trips", pattern.trips().stream().map(trip -> trip.trip_id).collect(Collectors.toList()))
                .addDetail("fallback", "straight line shape"));
        return RoutedPattern.of(pattern);
      }
    }

    private void store(RoutedPattern routedPattern) {
      storeShape(routedPattern);
      storeStopTimes(routedPattern);
    }

    private void storeShape(RoutedPattern routedPattern) {
      var shapePoints = transitFeed.internal().shape_points;
      String shapeId = routedPattern.pattern.trips().get(0).trip_id;

      for (Trip trip : routedPattern.pattern.trips()) {
        shapePoints.subMap(
            new Fun.Tuple2<>(trip.shape_id, -1),
            new Fun.Tuple2<>(trip.shape_id, Integer.MAX_VALUE)
        ).clear();
        trip.shape_id = shapeId;
        transitFeed.internal().trips.replace(trip.trip_id, trip);
      }

      for (ShapePoint shapePoint : routedPattern.shape.convertToShapePoints(shapeId)) {
        shapePoints.put(new Fun.Tuple2<>(shapeId, shapePoint.shape_pt_sequence), shapePoint);
      }
    }

    private void storeStopTimes(RoutedPattern routedPattern) {
      var storage = transitFeed.internal().stop_times;

      for (Trip trip : routedPattern.pattern.trips()) {
        List<StopTime> stopTimes = StreamSupport.stream(transitFeed.internal().getOrderedStopTimesForTrip(trip.trip_id).spliterator(), false).collect(Collectors.toList());

        for (int i = 0; i < stopTimes.size(); i++) {
          StopTime stopTime = stopTimes.get(i);
          stopTime.shape_dist_traveled = routedPattern.distances[i];
          storage.replace(new Fun.Tuple2<>(stopTime.trip_id, stopTime.stop_sequence), stopTime);
        }
      }
    }
  }


  @Value
  protected static class RoutedPattern {
    Pattern pattern;
    Shape shape;
    double[] distances;

    public static RoutedPattern of(Pattern pattern, RoutingResult routing) {
      double[] distances = DoubleStream.concat(
          DoubleStream.of(0.0),
          routing.getPathSegments().stream().mapToDouble(Path::getDistance)
      ).toArray();

      for (int i = 1; i < distances.length; i++) {
        distances[i] += distances[i - 1];
      }

      return new RoutedPattern(pattern, Shape.of(routing), distances);
    }

    public static RoutedPattern of(Pattern pattern) {
      Shape shape = Shape.of(Helper.toPointList(pattern.observations()));
      double[] distances = shape.convertToShapePoints("").stream()
          .mapToDouble(value -> value.shape_dist_traveled)
          .toArray();

      return new RoutedPattern(pattern, shape, distances);
    }
  }
}
