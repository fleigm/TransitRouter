package de.fleigm.ptmm.http.eval;

import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import de.fleigm.ptmm.Pattern;
import de.fleigm.ptmm.Shape;
import de.fleigm.ptmm.ShapeGenerator;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.routing.TransitRouter;
import de.fleigm.ptmm.util.StopWatch;
import lombok.Value;
import org.mapdb.Fun;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Dependent
public class GenerateNewGtfsFeed implements Function<EvaluationProcess, EvaluationProcess> {

  @Inject
  GraphHopper graphHopper;

  @Override
  public EvaluationProcess apply(EvaluationProcess evaluationProcess) {
    TransitFeed transitFeed = new TransitFeed(evaluationProcess.getPath() + Evaluation.ORIGINAL_GTFS_FEED);
    TransitRouter transitRouter = new TransitRouter(graphHopper, new PMap());
    ShapeGenerator shapeGenerator = new ShapeGenerator(transitFeed, transitRouter);

    AtomicInteger trips = new AtomicInteger(0);
    AtomicInteger generatedShapes = new AtomicInteger(0);

    StopWatch stopWatch = StopWatch.createAndStart();

    transitFeed.busRoutes()
        .values()
        .parallelStream()
        .flatMap(route -> transitFeed.findPatterns(route).stream())
        .map(pattern -> new PatternWitShape(pattern, shapeGenerator.generate(pattern)))
        .peek(patternWitShape -> generatedShapes.getAndIncrement())
        .peek(patternWitShape -> trips.getAndAdd(patternWitShape.pattern.trips().size()))
        .forEach(patternWitShape -> store(patternWitShape, transitFeed));

    transitFeed.internal().toFile(evaluationProcess.getPath() + Evaluation.GENERATED_GTFS_FEED);

    stopWatch.stop();

    evaluationProcess.getInfo()
        .addStatistic("trips", trips.intValue())
        .addStatistic("generatedShapes", generatedShapes.intValue())
        .addStatistic("executionTime.shapeGeneration", stopWatch.getMillis());

    return evaluationProcess;
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
