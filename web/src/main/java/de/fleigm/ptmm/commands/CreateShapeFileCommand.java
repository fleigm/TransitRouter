package de.fleigm.ptmm.commands;

import com.conveyal.gtfs.model.ShapePoint;
import com.graphhopper.GraphHopper;
import com.graphhopper.matching.Observation;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.cdi.Producers;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.TransitRouter;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Command(name = "generate", description = "generate missing shape files")
public class CreateShapeFileCommand implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(CreateShapeFileCommand.class);

  @CommandLine.ParentCommand
  EntryCommand entryCommand;

  @CommandLine.Parameters(
      index = "0",
      paramLabel = "GENERATED_GTFS_FILE",
      description = "Specifies where the generated gtfs feed is stored.")
  String generatedGtfsFile;

  private TransitFeed feed;
  private GraphHopper graphHopper;
  private TransitRouter transitRouter;
  private ConcurrentNavigableMap<Fun.Tuple2<String, Integer>, ShapePoint> shapePoints;

  @Override
  public void run() {
    StopWatch stopWatch = new StopWatch();

    logger.info("Start initialization.");
    stopWatch.start();
    init();
    stopWatch.stop();
    logger.info("Finished initialization. ({} ms)", stopWatch.getMillis());

    logger.info("Start shape generation.");
    stopWatch.start();
    createShapes();
    stopWatch.stop();
    logger.info("Finished shape generation. ({} ms)", stopWatch.getMillis());

    logger.info("Writing new gtfs feed.");
    stopWatch.start();
    saveGtfsFeed();
    stopWatch.stop();
    logger.info("Finished new gtfs feed. ({} ms)", stopWatch.getMillis());
  }

  private void init() {
    Producers producers = new Producers();
    graphHopper = producers.graphHopper(entryCommand.osmFile, entryCommand.tempFolder, entryCommand.clean);
    feed = producers.transitFeed(entryCommand.gtfsFile);
    transitRouter = new TransitRouter(graphHopper, new PMap().putObject("profile", "bus_shortest"));
    this.shapePoints = feed.internal().shape_points;
  }

  private void saveGtfsFeed() {
    feed.internal().toFile(generatedGtfsFile);
  }

  private void createShapes() {
    logger.info("Generate shapes for {} trips", feed.trips().size());
    StopWatch stopWatch = new StopWatch();
    LongSummaryStatistics statistics = new LongSummaryStatistics();

    for (String id : feed.trips().keySet()) {
      stopWatch.start();
      createAndSetShapeForTrip(id, id);
      stopWatch.stop();
      logger.debug("Shape generation for trip {} took {}ms", id, stopWatch.getMillis());
      statistics.accept(stopWatch.getMillis());
    }

    logger.info("count: {} \ntotal duration: {}ms \navg duration: {}ms \nmin duration {}ms \nmax duration {}ms",
        statistics.getCount(),
        statistics.getSum(),
        statistics.getAverage(),
        statistics.getMin(),
        statistics.getMax());

  }

  private void createAndSetShapeForTrip(String shapeId, String tripId) {
    List<ShapePoint> shape = createShapeForTrip(shapeId, tripId);

    feed.trips().get(tripId).shape_id = shapeId;

    for (ShapePoint shapePoint : shape) {
      this.shapePoints.put(new Fun.Tuple2<>(shapeId, shapePoint.shape_pt_sequence), shapePoint);
    }
  }

  private List<ShapePoint> createShapeForTrip(String shapeId, String tripId) {
    List<Observation> observations = feed.getOrderedStopsForTrip(tripId)
        .stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());

    RoutingResult route = transitRouter.route(observations);

    PointList points = route.getPath().calcPoints();

    List<ShapePoint> shapePoints = new ArrayList<>(points.getSize());
    for (int i = 0; i < points.size(); i++) {
      shapePoints.add(new ShapePoint(shapeId, points.getLat(i), points.getLon(i), i, 0.0));
    }

    return shapePoints;

  }
}
