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
        .putObject("measurement_error_sigma", info.getParameters().getAlpha())
        .putObject("candidate_search_radius", info.getParameters().getCandidateSearchRadius())
        .putObject("beta", info.getParameters().getBeta())
        .putObject("u_turn_distance_penalty", info.getParameters().getUTurnDistancePenalty()));
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

    transitFeed.internal().toFile(info.fullPath(evaluationFolder).resolve(Evaluation.GENERATED_GTFS_FEED).toString());

    stopWatch.stop();

    info.addStatistic("trips", trips.intValue())
        .addStatistic("generatedShapes", generatedShapes.intValue())
        .addStatistic("executionTime.shapeGeneration", stopWatch.getMillis());
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
