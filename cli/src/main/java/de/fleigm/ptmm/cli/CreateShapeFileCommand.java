package de.fleigm.ptmm.cli;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import de.fleigm.ptmm.Shape;
import de.fleigm.ptmm.ShapeGenerator;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.Pattern;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.CustomGraphHopper;
import de.fleigm.ptmm.routing.TransitRouter;
import de.fleigm.ptmm.util.StopWatch;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

@Command(name = "generate", description = "generate missing shape files")
public class CreateShapeFileCommand implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(CreateShapeFileCommand.class);


  @Option(
      names = {"-t", "--temp"},
      paramLabel = "TEMP_FOLDER",
      description = "Folder for temporary files",
      defaultValue = "gh")
  String tempFolder;

  @Option(
      names = {"-c", "--clean"},
      paramLabel = "CLEAN",
      description = "Clean temporary files.")
  boolean clean;

  @Option(
      names = {"-D", "--drop"},
      paramLabel = "DROP_SHAPES",
      description = "Drop already existing shapes."
  )
  boolean dropExistingShapes;

  @Parameters(
      index = "0",
      paramLabel = "OSM_FILE",
      description = "OSM file")
  String osmFile;

  @Parameters(
      index = "1",
      paramLabel = "GTFS_FILE",
      description = "GTFS file")
  String gtfsFile;

  @Parameters(
      index = "2",
      paramLabel = "GENERATED_GTFS_FILE",
      description = "new GTFS file with generated shapes")
  String generatedGtfsFile;

  private TransitFeed feed;
  private GraphHopper graphHopper;
  private TransitRouter transitRouter;
  private ShapeGenerator shapeGenerator;
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
    graphHopper = loadGraphHopper(osmFile, tempFolder, clean);
    feed = new TransitFeed(gtfsFile);

    PMap transitRouterOptions = new PMap()
        .putObject("profile", "bus_custom_shortest")
        .putObject("candidate_search_radius", 25);
    transitRouter = new TransitRouter(graphHopper, transitRouterOptions);
    shapeGenerator = new ShapeGenerator(feed, transitRouter);

    this.shapePoints = feed.internal().shape_points;
  }

  private GraphHopper loadGraphHopper(String osmFile, String storagePath, boolean cleanTemporaryFiles) {
    if (cleanTemporaryFiles) {
      try {
        Files.deleteIfExists(Path.of(storagePath));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    PMap busFlagEncoderOptions = new PMap().putObject(com.graphhopper.util.Parameters.Routing.TURN_COSTS, true);

    GraphHopper graphHopper = new CustomGraphHopper()
        .forServer()
        .setGraphHopperLocation(storagePath)
        .setEncodingManager(EncodingManager.create(new BusFlagEncoder(busFlagEncoderOptions)))
        .setProfiles(
            new Profile("bus_fastest").setVehicle("bus").setWeighting("fastest").setTurnCosts(true),
            new Profile("bus_custom_shortest").setVehicle("bus").setWeighting("custom_shortest").setTurnCosts(true),
            new Profile("bus_shortest").setVehicle("bus").setWeighting("shortest").setTurnCosts(true));

    graphHopper.setDataReaderFile(osmFile);

    graphHopper.importOrLoad();

    return graphHopper;
  }

  private void saveGtfsFeed() {
    feed.internal().toFile(generatedGtfsFile);
  }

  private void createShapes() {
    feed.busRoutes()
        .values()
        .parallelStream()
        .forEach(this::createShapesForRoute);
  }

  private void createShapesForRoute(Route route) {
    List<Pattern> patterns = feed.findPatterns(route);

    patterns.forEach(this::createShapeForPattern);

    int totalTrips = patterns.stream()
        .mapToInt(pattern -> pattern.trips().size())
        .sum();

    logger.info("Generated shapes for route {}:\n\t route_id: {} \t trips: {} \t shapes: {}",
        route.route_short_name,
        route.route_id,
        totalTrips,
        patterns.size());
  }

  private void createShapeForPattern(Pattern pattern) {
    String shapeId = pattern.trips().get(0).trip_id;
    Shape shape = shapeGenerator.generate(pattern);
    List<ShapePoint> shapePoints = shape.convertToShapePoints(shapeId);

    for (Trip trip : pattern.trips()) {
      deleteOldShape(trip);
      trip.shape_id = shapeId;
      feed.internal().trips.replace(trip.trip_id, trip);
    }

    for (ShapePoint shapePoint : shapePoints) {
      this.shapePoints.put(new Fun.Tuple2<>(shapeId, shapePoint.shape_pt_sequence), shapePoint);
    }
  }

  private void deleteOldShape(Trip trip) {
    shapePoints.subMap(new Fun.Tuple2(trip.shape_id, null), new Fun.Tuple2(trip.shape_id, Fun.HI)).clear();
    trip.shape_id = null;
  }
}
