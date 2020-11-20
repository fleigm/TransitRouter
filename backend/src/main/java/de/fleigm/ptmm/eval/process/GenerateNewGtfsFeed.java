package de.fleigm.ptmm.eval.process;

import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mapdb.Fun;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Dependent
public class GenerateNewGtfsFeed implements Consumer<Info> {

  private final GraphHopper graphHopper;
  private final String evaluationFolder;

  @Inject
  public GenerateNewGtfsFeed(GraphHopper graphHopper,
                             @ConfigProperty(name = "evaluation.folder") String evaluationFolder) {
    this.graphHopper = graphHopper;
    this.evaluationFolder = evaluationFolder;
  }

  @Override
  public void accept(Info info) {
    TransitFeed transitFeed = new TransitFeed(info.fullPath(evaluationFolder).resolve(Evaluation.ORIGINAL_GTFS_FEED));
    TransitRouter transitRouter = new TransitRouter(graphHopper, new PMap()
        .putObject("profile", info.getParameters().getProfile())
        .putObject("measurement_error_sigma", info.getParameters().getSigma())
        .putObject("candidate_search_radius", info.getParameters().getCandidateSearchRadius())
        .putObject("beta", info.getParameters().getBeta()));
    ShapeGenerator shapeGenerator = new ShapeGenerator(transitRouter);

    AtomicInteger trips = new AtomicInteger(0);
    AtomicInteger generatedShapes = new AtomicInteger(0);

    StopWatch stopWatch = StopWatch.createAndStart();

    transitFeed.busRoutes()
        .values()
        .parallelStream()
        .flatMap(route -> transitFeed.findPatterns(route).stream())
        .map(pattern -> generateShape(pattern, info, shapeGenerator))
        .peek(patternWitShape -> generatedShapes.getAndIncrement())
        .peek(patternWitShape -> trips.getAndAdd(patternWitShape.pattern.trips().size()))
        .forEach(patternWitShape -> store(patternWitShape, transitFeed));

    transitFeed.internal().toFile(info.fullPath(evaluationFolder).resolve(Evaluation.GENERATED_GTFS_FEED).toString());

    stopWatch.stop();

    info.addStatistic("trips", trips.intValue())
        .addStatistic("generatedShapes", generatedShapes.intValue())
        .addStatistic("executionTime.shapeGeneration", stopWatch.getMillis());
  }

  private PatternWitShape generateShape(Pattern pattern, Info info, ShapeGenerator shapeGenerator) {
    try {
      return new PatternWitShape(pattern, shapeGenerator.generate(pattern));
    } catch (Exception e) {
      info.addError(
          Error.of("shape_generation_failed", "shape generation failed.", e)
              .addDetail("route", pattern.route())
              .addDetail("trips", pattern.trips().stream().map(trip -> trip.trip_id).collect(Collectors.toList()))
              .addDetail("fallback", "straight line shape"));

      PointList points = new PointList();
      for (Stop stop : pattern.stops()) {
        points.add(stop.stop_lat, stop.stop_lon);
      }

      return new PatternWitShape(pattern, new Shape(points));
    }
  }

  private void store(PatternWitShape patternWitShape, TransitFeed transitFeed) {
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

  @Value
  private static class PatternWitShape {
    private final Pattern pattern;
    private final Shape shape;

  }
}
