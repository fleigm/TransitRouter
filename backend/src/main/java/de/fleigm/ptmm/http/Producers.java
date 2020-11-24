package de.fleigm.ptmm.http;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.TransitRouter;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class Producers {

  @Produces
  //@Startup
  @ApplicationScoped
  public TransitFeed transitFeed(@ConfigProperty(name = "routing.gtfs.feed-file") String filePath) {
    return new TransitFeed(filePath);
  }

  @Produces
  @Startup
  @ApplicationScoped
  public GraphHopper graphHopper(
      @ConfigProperty(name = "routing.graphHopper.osm-file") String osmFile,
      @ConfigProperty(name = "routing.graphHopper.location") String storagePath,
      @ConfigProperty(name = "routing.graphHopper.clean") boolean cleanTemporaryFiles) {

    if (cleanTemporaryFiles) {
      try {
        Files.deleteIfExists(Path.of(storagePath));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    PMap busFlagEncoderOptions = new PMap().putObject(Parameters.Routing.TURN_COSTS, true);

    GraphHopper graphHopper = new GraphHopperOSM()
        .forServer()
        .setGraphHopperLocation(storagePath)
        .setEncodingManager(EncodingManager.create(new BusFlagEncoder(busFlagEncoderOptions)))
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

  @Produces
  public TransitRouter transitRouter(GraphHopper graphHopper) {
    return new TransitRouter(graphHopper, new PMap());
  }
}
