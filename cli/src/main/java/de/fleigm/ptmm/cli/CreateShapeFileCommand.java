package de.fleigm.ptmm.cli;

import com.conveyal.gtfs.model.ShapePoint;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.matching.Observation;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.CustomGraphHopper;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.TransitRouter;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.stream.Collectors;

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
    List<Trip> busTrips = getBusTrips();

    // remove bus shapes
    for (Trip busTrip : busTrips) {
      shapePoints.subMap(new Fun.Tuple2(busTrip.shape_id, null), new Fun.Tuple2(busTrip.shape_id, Fun.HI)).clear();
      busTrip.shape_id = busTrip.trip_id;
    }

    logger.info("Generate shapes for {} trips", busTrips.size());

    busTrips.parallelStream().forEach(this::createAndSetShapeForTrip);

    // reinsert trips otherwise the changes wont be stored in the file
    Map<String, Trip> trips = feed.internal().trips;
    busTrips.forEach(trip -> trips.replace(trip.trip_id, trip));
  }

  private List<Trip> getBusTrips() {
    HashSet<String> busRoutes = feed.routes().values().stream()
        .filter(route -> route.route_type == 3)
        .map(route -> route.route_id)
        .collect(Collectors.toCollection(HashSet::new));

    return feed.trips().values().stream()
        .filter(trip -> busRoutes.contains(trip.route_id))
        .collect(Collectors.toList());
  }

  private void createAndSetShapeForTrip(Trip trip) {
    trip.shape_id = trip.trip_id;
    List<ShapePoint> shape = createShapeForTrip(trip.shape_id, trip.trip_id);

    for (ShapePoint shapePoint : shape) {
      this.shapePoints.put(new Fun.Tuple2<>(trip.shape_id, shapePoint.shape_pt_sequence), shapePoint);
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
