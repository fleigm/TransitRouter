package de.fleigm.ptmm.commands;

import com.conveyal.gtfs.model.ShapePoint;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.RoutingService;
import de.fleigm.ptmm.routing.TransitRouter;
import org.mapdb.Fun;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Command(name = "generate", description = "generate missing shape files")
public class CreateShapeFileCommand implements Runnable {

  @CommandLine.ParentCommand
  EntryCommand entryCommand;

  @CommandLine.Parameters(
      index = "0",
      paramLabel = "GENERATED_GTFS_FILE",
      description = "Specifies where the generated gtfs feed is stored.")
  String generatedGtfsFile;

  private TransitFeed feed;
  private GraphHopper graphHopper;
  private RoutingService routingService;
  private TransitRouter transitRouter;

  @Override
  public void run() {
    init();
    createShapes();
    saveGtfsFeed();
  }

  private void init() {
    graphHopper = createGraphHopper();
    feed = new TransitFeed(entryCommand.gtfsFile);
    transitRouter = new TransitRouter(graphHopper, new PMap().putObject("profile", "bus_shortest"));
    routingService = new RoutingService(feed, transitRouter);
  }

  private GraphHopper createGraphHopper() {
    GraphHopper graphHopper = new GraphHopperOSM()
        .forServer()
        .setGraphHopperLocation(entryCommand.tempFolder)
        .setEncodingManager(EncodingManager.create(new BusFlagEncoder()))
        .setProfiles(
            new Profile("bus_fastest").setVehicle("bus").setWeighting("fastest"),
            new Profile("bus_shortest").setVehicle("bus").setWeighting("shortest"));

    graphHopper.setDataReaderFile(entryCommand.osmFile);

    graphHopper.importOrLoad();

    return graphHopper;
  }

  private void saveGtfsFeed() {
    feed.internal().toFile(generatedGtfsFile);
  }

  private void createShapes() {
    feed.internal().trips.keySet()
        .stream()
        .limit(100)
        .forEach(id -> createAndSetShapeForTrip(id, id));
  }

  private void createAndSetShapeForTrip(String shapeId, String tripId) {
    List<ShapePoint> shapePoints = createShapeForTrip(shapeId, tripId);

    feed.internal().trips.get(tripId).shape_id = shapeId;

    feed.internal().shape_points.putAll(
        shapePoints.stream()
            .collect(
                Collectors.toMap(
                    shapePoint -> new Fun.Tuple2<>(shapeId, shapePoint.shape_pt_sequence),
                    Function.identity())));
  }

  private List<ShapePoint> createShapeForTrip(String shapeId, String tripId) {
    RoutingResult route = routingService.routeTrip(tripId);

    PointList points = route.getPath().calcPoints();

    List<ShapePoint> shapePoints = new ArrayList<>(points.getSize());
    for (int i = 0; i < points.size(); i++) {
      shapePoints.add(new ShapePoint(shapeId, points.getLat(i), points.getLon(i), i, 0.0));
    }

    return shapePoints;

  }
}
