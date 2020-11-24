package de.fleigm.ptmm.eval.process;

import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import de.fleigm.ptmm.Pattern;
import de.fleigm.ptmm.Shape;
import de.fleigm.ptmm.ShapeGenerator;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Error;
import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.routing.TransitRouter;
import de.fleigm.ptmm.util.StopWatch;
import lombok.Value;
import org.mapdb.Fun;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Dependent
public class GenerateNewGtfsFeed implements Consumer<Info> {

  private final GraphHopper graphHopper;

  @Inject
  public GenerateNewGtfsFeed(GraphHopper graphHopper) {
    this.graphHopper = graphHopper;
  }

  @Override
  public void accept(Info info) {
    TransitFeed transitFeed = new TransitFeed(info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED));
    ShapeGenerator shapeGenerator = new ShapeGenerator(
        new TransitRouter(graphHopper, new PMap()
            .putObject("profile", info.getParameters().getProfile())
            .putObject("measurement_error_sigma", info.getParameters().getSigma())
            .putObject("candidate_search_radius", info.getParameters().getCandidateSearchRadius())
            .putObject("beta", info.getParameters().getBeta())));
    ShapeGenerator fallBackShapeGenerator = new ShapeGenerator(
        new TransitRouter(graphHopper, new PMap()
            .putObject("profile", info.getParameters().getProfile())
            .putObject("measurement_error_sigma", info.getParameters().getSigma())
            .putObject("candidate_search_radius", info.getParameters().getCandidateSearchRadius())
            .putObject("beta", info.getParameters().getBeta())
            .putObject("disable_turn_costs", true)));

    new Runner(info, transitFeed, shapeGenerator, fallBackShapeGenerator).run();
  }

  private static class Runner {
    private final Info info;
    private final TransitFeed transitFeed;
    private final ShapeGenerator shapeGenerator;
    private final ShapeGenerator fallbackShapeGenerator;

    public Runner(Info info,
                  TransitFeed transitFeed,
                  ShapeGenerator shapeGenerator,
                  ShapeGenerator fallbackShapeGenerator) {
      this.info = info;
      this.transitFeed = transitFeed;
      this.shapeGenerator = shapeGenerator;
      this.fallbackShapeGenerator = fallbackShapeGenerator;
    }

    void run() {
      AtomicInteger trips = new AtomicInteger(0);
      AtomicInteger generatedShapes = new AtomicInteger(0);

      StopWatch stopWatch = StopWatch.createAndStart();

      transitFeed.busRoutes()
          .values()
          .parallelStream()
          .flatMap(route -> transitFeed.findPatterns(route).stream())
          .map(this::generateShape)
          .peek(patternWitShape -> generatedShapes.getAndIncrement())
          .peek(patternWitShape -> trips.getAndAdd(patternWitShape.pattern.trips().size()))
          .forEach(this::store);

      transitFeed.internal().toFile(info.getPath().resolve(Evaluation.GENERATED_GTFS_FEED).toString());

      stopWatch.stop();

      info.addStatistic("trips", trips.intValue())
          .addStatistic("generatedShapes", generatedShapes.intValue())
          .addStatistic("executionTime.shapeGeneration", stopWatch.getMillis());
    }

    private PatternWitShape generateShape(Pattern pattern) {
      try {
        return new PatternWitShape(pattern, shapeGenerator.generate(pattern));
      } catch (Exception e) {
        info.addError(
            Error.of("shape_generation_failed", "shape generation failed, use fallback shape generator.", e)
                .addDetail("route", pattern.route())
                .addDetail("trips", pattern.trips().stream().map(trip -> trip.trip_id).collect(Collectors.toList()))
                .addDetail("fallback", "straight line shape"));

        return new PatternWitShape(pattern, fallbackShapeGenerator.generate(pattern));

        /*PointList points = new PointList();
        for (Stop stop : pattern.stops()) {
          points.add(stop.stop_lat, stop.stop_lon);
        }

        return new PatternWitShape(pattern, new Shape(points));*/
      }
    }

    private void store(PatternWitShape patternWitShape) {
      var shapePoints = transitFeed.internal().shape_points;
      String shapeId = patternWitShape.pattern.trips().get(0).trip_id;

      for (Trip trip : patternWitShape.pattern.trips()) {
        shapePoints.subMap(
            new Fun.Tuple2<>(trip.shape_id, -1),
            new Fun.Tuple2<>(trip.shape_id, Integer.MAX_VALUE)
        ).clear();
        trip.shape_id = shapeId;
        transitFeed.internal().trips.replace(trip.trip_id, trip);
      }

      for (ShapePoint shapePoint : patternWitShape.shape.convertToShapePoints(shapeId)) {
        shapePoints.put(new Fun.Tuple2<>(shapeId, shapePoint.shape_pt_sequence), shapePoint);
      }
    }
  }


  @Value
  private static class PatternWitShape {
    Pattern pattern;
    Shape shape;
  }
}
