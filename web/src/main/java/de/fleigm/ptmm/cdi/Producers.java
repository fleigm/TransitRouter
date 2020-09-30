package de.fleigm.ptmm.cdi;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.RoutingService;
import de.fleigm.ptmm.routing.TransitRouter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class Producers {

  @Cli
  @Produces
  public TransitFeed transitFeedFromCli(CommandLine.ParseResult parseResult) {
    if (!parseResult.hasMatchedOption("--gtfs-file")) {
      throw new RuntimeException("Missing gtfs feed argument");
    }

    String filePath = parseResult.matchedOption("--gtfs-file").getValue();

    return new TransitFeed(filePath);
  }

  @Default
  @Produces
  public TransitFeed transitFeed(@ConfigProperty(name = "routing.gtfs.feed-file") String filePath) {
    return new TransitFeed(filePath);
  }

  @Cli
  @Produces
  public GraphHopper graphHopperFromCli(CommandLine.ParseResult parseResult) {
    String osmFile = parseResult.matchedOption("--osm-file").getValue();
    String storagePath = parseResult.matchedOptionValue("--temp", "gh");
    boolean cleanTemporaryFiles = parseResult.matchedOptionValue("c", false);

    return graphHopper(osmFile, storagePath, cleanTemporaryFiles);
  }

  @Produces
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
            new Profile("bus_fastest").setVehicle("bus").setWeighting("fastest").setTurnCosts(true),
            new Profile("bus_shortest").setVehicle("bus").setWeighting("shortest").setTurnCosts(true));

    graphHopper.setDataReaderFile(osmFile);

    graphHopper.importOrLoad();

    return graphHopper;
  }

  @Produces
  public TransitRouter transitRouter(GraphHopper graphHopper) {
    return new TransitRouter(graphHopper, new PMap().putObject("profile", "bus_fastest"));
  }
}
