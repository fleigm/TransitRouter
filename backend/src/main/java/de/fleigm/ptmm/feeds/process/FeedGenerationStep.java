package de.fleigm.ptmm.feeds.process;

import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.StopTime;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.routing.Path;
import de.fleigm.ptmm.Pattern;
import de.fleigm.ptmm.Shape;
import de.fleigm.ptmm.feeds.Error;
import de.fleigm.ptmm.feeds.GeneratedFeed;
import de.fleigm.ptmm.feeds.api.TransitRouterFactory;
import de.fleigm.ptmm.gtfs.Feed;
import de.fleigm.ptmm.gtfs.TransitFeed;
import de.fleigm.ptmm.routing.Observation;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.TransitRouter;
import de.fleigm.ptmm.util.StopWatch;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.Fun;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    TransitRouter transitRouter = transitRouterFactory.create(info);
    TransitRouter transitRouterWithoutTurnRestrictions = transitRouterFactory.create(
        info.getParameters()
            .toPropertyMap()
            .putObject("disable_turn_costs", true));

    List<TransitRouter> shapeGenerators = List.of(transitRouter, transitRouterWithoutTurnRestrictions);

    new Runner(info, transitFeed, shapeGenerators).run();
  }

  private static class Runner {
    private final GeneratedFeed info;
    private final TransitFeed transitFeed;
    private final List<TransitRouter> routers;

    public Runner(GeneratedFeed info, TransitFeed transitFeed, List<TransitRouter> routers) {
      this.info = info;
      this.transitFeed = transitFeed;
      this.routers = routers;
    }

    void run() {
      log.info("Start feed generation step.");

      AtomicInteger trips = new AtomicInteger(0);
      AtomicInteger generatedShapes = new AtomicInteger(0);

      StopWatch stopWatch = StopWatch.createAndStart();

      transitFeed.busRoutes()
          .values()
          .parallelStream()
          .flatMap(route -> transitFeed.findPatterns(route).stream())
          .map(this::generateShape)
          .peek(routedPattern -> generatedShapes.getAndIncrement())
          .peek(routedPattern -> trips.getAndAdd(routedPattern.pattern.trips().size()))
          .forEach(this::store);

      info.setFeed(
          Feed.createFromTransitFeed(
              info.getFileStoragePath().resolve(GeneratedFeed.GENERATED_GTFS_FEED),
              transitFeed));

      stopWatch.stop();

      info.addStatistic("trips", trips.intValue())
          .addStatistic("generatedShapes", generatedShapes.intValue())
          .addStatistic("executionTime.shapeGeneration", stopWatch.getMillis());

      log.info("Finished feed generation step. Took {}s", stopWatch.getSeconds());
    }

    private RoutedPattern generateShape(Pattern pattern) {
      List<Observation> observations = pattern.observations();

      for (var router : routers) {
        try {
          return new RoutedPattern(pattern, router.route(observations));
        } catch (Exception exception) {
          info.addError(
              Error.of(
                  "shape_generation_failed",
                  "shape generation failed, use fallback shape generator.",
                  exception
              )
                  .addDetail("route", pattern.route())
                  .addDetail("trips", pattern.trips().stream().map(trip -> trip.trip_id).collect(Collectors.toList()))
                  .addDetail("fallback", "straight line shape"));
        }
      }

      throw new RuntimeException("All shape generators failed!");
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

      for (ShapePoint shapePoint : Shape.of(routedPattern.route).convertToShapePoints(shapeId)) {
        shapePoints.put(new Fun.Tuple2<>(shapeId, shapePoint.shape_pt_sequence), shapePoint);
      }
    }

    private void storeStopTimes(RoutedPattern routedPattern) {
      var storage = transitFeed.internal().stop_times;

      double[] distances = DoubleStream.concat(
          DoubleStream.of(0.0),
          routedPattern.route.getPathSegments().stream().mapToDouble(Path::getDistance)
      ).toArray();

      for (int i = 1; i < distances.length; i++) {
        distances[i] += distances[i - 1];
      }

      for (Trip trip : routedPattern.pattern.trips()) {
        List<StopTime> stopTimes = StreamSupport.stream(transitFeed.internal().getOrderedStopTimesForTrip(trip.trip_id).spliterator(), false).collect(Collectors.toList());

        for (int i = 0; i < stopTimes.size(); i++) {
          StopTime stopTime = stopTimes.get(i);
          if (i >= distances.length) {
            log.info("failed");
          }
          stopTime.shape_dist_traveled = distances[i];
          storage.replace(new Fun.Tuple2<>(stopTime.trip_id, stopTime.stop_sequence), stopTime);
        }
      }


    }
  }


  @Value
  private static class RoutedPattern {
    Pattern pattern;
    RoutingResult route;
  }
}
