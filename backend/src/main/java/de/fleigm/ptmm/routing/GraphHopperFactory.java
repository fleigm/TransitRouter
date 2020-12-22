package de.fleigm.ptmm.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import io.quarkus.runtime.Startup;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.nio.file.Path;

@ApplicationScoped
public class GraphHopperFactory {

  @Produces
  @Startup
  @ApplicationScoped
  public GraphHopper create(
      @ConfigProperty(name = "routing.graphHopper.osm-file") String osmFile,
      @ConfigProperty(name = "routing.graphHopper.location") String storagePath,
      @ConfigProperty(name = "routing.graphHopper.clean") boolean cleanTemporaryFiles) {

    if (cleanTemporaryFiles) {
      try {
        FileUtils.deleteDirectory(Path.of(storagePath).toFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    BusFlagEncoder busFlagEncoder = new BusFlagEncoder(new PMap()
        .putObject(Parameters.Routing.TURN_COSTS, true)
        .putObject("block_barriers", false));

    EncodingManager encodingManager = EncodingManager.start()
        .add(busFlagEncoder)
        .addRelationTagParser(new BusNetworkRelationTagParser())
        .build();

    GraphHopper graphHopper = new GraphHopperOSM()
        .forServer()
        .setGraphHopperLocation(storagePath)
        .setEncodingManager(encodingManager)
        .setProfiles(
            new Profile("bus_fastest").setVehicle("bus").setWeighting("fastest").setTurnCosts(false),
            new Profile("bus_fastest_turn").setVehicle("bus").setWeighting("fastest").setTurnCosts(true),
            new Profile("bus_shortest_turn").setVehicle("bus").setWeighting("shortest").setTurnCosts(true),
            new Profile("bus_shortest").setVehicle("bus").setWeighting("shortest").setTurnCosts(false)
        );

    graphHopper.setDataReaderFile(osmFile);

    graphHopper.importOrLoad();

    return graphHopper;
  }
}
