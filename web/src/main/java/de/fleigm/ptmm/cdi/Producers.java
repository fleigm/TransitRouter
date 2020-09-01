package de.fleigm.ptmm.cdi;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.RoutingService;
import picocli.CommandLine;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class Producers {

  @Produces
  @ApplicationScoped
  public TransitFeed transitFeed(CommandLine.ParseResult parseResult) {
    if (!parseResult.hasMatchedOption("--gtfs-file")) {
      throw new RuntimeException("Missing gtfs feed argument");
    }

    String filePath = parseResult.matchedOption("--gtfs-file").getValue();

    return new TransitFeed(filePath);
  }

  @Produces
  public GraphHopper graphHopper(CommandLine.ParseResult parseResult) {
    String osmFile = parseResult.matchedOption("--osm-file").getValue();
    String storagePath = parseResult.matchedOptionValue("--temp", "gh");
    boolean cleanTemporaryFiles = parseResult.matchedOptionValue("c", false);

    if (cleanTemporaryFiles) {
      try {
        Files.deleteIfExists(Path.of(storagePath));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    GraphHopper graphHopper = new GraphHopperOSM()
        .forServer()
        .setGraphHopperLocation(storagePath)
        .setEncodingManager(EncodingManager.create(new BusFlagEncoder()))
        .setProfiles(
            new Profile("bus_fastest").setVehicle("bus").setWeighting("fastest"),
            new Profile("bus_shortest").setVehicle("bus").setWeighting("shortest"));

    graphHopper.setDataReaderFile(osmFile);

    graphHopper.importOrLoad();

    return graphHopper;
  }
}
