package de.fleigm.transitrouter.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.OSMTurnRelationParser;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import io.quarkus.runtime.Startup;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@ApplicationScoped
public class GraphHopperFactory {

  /**
   * Create a new GraphHopper instance from a given OSM file.
   *
   * @param osmFile             osm file
   * @param storagePath         storage file for GraphHopper cache
   * @param cleanTemporaryFiles clear old cache and reprocess osm file.
   * @return GraphHopper instance
   */
  @Produces
  @Startup
  @ApplicationScoped
  public GraphHopper create(
      @ConfigProperty(name = "app.gh.osm") Path osmFile,
      @ConfigProperty(name = "app.storage") Path storagePath,
      @ConfigProperty(name = "app.gh.clean") boolean cleanTemporaryFiles) {

    Path osmStoragePath = storagePath.resolve(osmFile.getFileName());

    if (cleanTemporaryFiles) {
      try {
        FileUtils.deleteDirectory(osmStoragePath.toFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    BusFlagEncoder busFlagEncoder = new BusFlagEncoder(new PMap()
        .putObject(Parameters.Routing.TURN_COSTS, true)
        .putObject("block_barriers", true));

    RailFlagEncoder railFlagEncoder = new RailFlagEncoder(new PMap());

    OSMTurnRelationParser osmTurnRelationParser = new OSMTurnRelationParser(
        "bus",
        1,
        List.of("bus", "motorcar", "psv", "motor_vehicle", "vehicle", "access"));

    EncodingManager encodingManager = EncodingManager.start()
        .add(busFlagEncoder)
        .add(railFlagEncoder)
        .addRelationTagParser(new BusNetworkRelationTagParser())
        .addTurnCostParser(osmTurnRelationParser)
        .build();

    GraphHopper graphHopper = new GraphHopperOSM()
        .forServer()
        .setGraphHopperLocation(osmStoragePath.toString())
        .setEncodingManager(encodingManager)
        .setProfiles(
            new Profile("bus_fastest")
                .setVehicle("bus")
                .setWeighting("fastest")
                .setTurnCosts(true),
            new Profile("bus_shortest")
                .setVehicle("bus")
                .setWeighting("shortest")
                .setTurnCosts(true),
            new Profile("rail")
                .setVehicle("rail")
                .setWeighting("shortest")
                .setTurnCosts(false)
        );

    graphHopper.setDataReaderFile(osmFile.toString());

    graphHopper.importOrLoad();

    return graphHopper;
  }
}
