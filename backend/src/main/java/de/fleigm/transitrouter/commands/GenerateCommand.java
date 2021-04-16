package de.fleigm.transitrouter.commands;

import com.graphhopper.GraphHopper;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.feeds.api.TransitRouterFactory;
import de.fleigm.transitrouter.feeds.evaluation.Evaluation;
import de.fleigm.transitrouter.feeds.evaluation.FeedEvaluationStep;
import de.fleigm.transitrouter.feeds.evaluation.Shapevl;
import de.fleigm.transitrouter.feeds.process.ShapeGenerationStep;
import de.fleigm.transitrouter.gtfs.Feed;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.http.json.ObjectMapperConfiguration;
import de.fleigm.transitrouter.routing.GraphHopperFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CommandLine.Command(name = "generate", description = "generate shapes for a GTFS feed")
public class GenerateCommand implements Runnable {
  @Option(names = {"-x"}, description = "path to osm file in osm.pbf format", required = true)
  Path osmFile;

  @Option(names = {"-s", "--sigma"}, defaultValue = "10.0", description = "tuning parameter sigma")
  double sigma;

  @Option(names = {"-b", "--beta"}, defaultValue = "1.0", description = "tuning parameter beta")
  double beta;

  @Option(names = {"-r", "--router"}, defaultValue = "tr", description = "router")
  String router;

  @Option(names = {"-m", "--mot"}, defaultValue = "all", split = ",",
      description = "means of transportation. Default: ${DEFAULT-VALUE}")
  String[] mot;

  @Option(names = {"-e", "--evaluate"}, description = "evaluate results")
  boolean evaluate;

  @CommandLine.Parameters()
  Path gtfsFeed;

  Path storage;
  GeneratedFeed generatedFeed;


  @Override
  public void run() {
    try {
      if (storage == null) {
        init();
      }
      generateFeed();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("failed");
      System.exit(1);
    }
  }

  public void init() {
    try {
      storage = Files.createTempDirectory(UUID.randomUUID().toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() ->
        FileUtils.deleteQuietly(storage.toFile())));
    System.setProperty("app.storage", storage.toString());
  }

  private void generateFeed() throws IOException {
    Feed feed = prepareFeed(gtfsFeed);

    GraphHopper graphHopper = new GraphHopperFactory().create(osmFile, storage, false);
    TransitRouterFactory transitRouterFactory = new TransitRouterFactory.Default(graphHopper);
    Shapevl shapevl = new Shapevl(
        ConfigProvider.getConfig().getValue("app.evaluation-tool", Path.class));

    generatedFeed = GeneratedFeed.builder()
        .name("")
        .originalFeed(feed)
        .parameters(buildParameterMap())
        .build();

    GeneratedFeedRepository generatedFeeds = new GeneratedFeedRepository(
        storage,
        ObjectMapperConfiguration.get());

    generatedFeeds.save(generatedFeed);

    new ShapeGenerationStep(transitRouterFactory).run(generatedFeed);

    Files.copy(
        generatedFeed.getFeed().getPath(),
        gtfsFeed.resolveSibling(
            FilenameUtils.removeExtension(gtfsFeed.getFileName().toString()) + ".generated.zip"));

    if (evaluate) {
      new FeedEvaluationStep(shapevl).run(generatedFeed);

      Evaluation evaluation = generatedFeed.getExtension(Evaluation.class).get();

      Files.copy(evaluation.getReport(), gtfsFeed.resolveSibling(evaluation.getReport().getFileName()));
    }


  }

  private Feed prepareFeed(Path originalFeed) {
    try (var inputStream = new FileInputStream(originalFeed.toFile())) {
      return Feed.create(storage.resolve(originalFeed.getFileName()), inputStream);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Map<Type, Parameters> buildParameterMap() {
    Set<String> mot = Set.of(this.mot);

    Parameters railProfile = Parameters.builder()
        .profile("rail")
        .sigma(sigma)
        .candidateSearchRadius(sigma)
        .beta(beta)
        .useGraphHopperMapMatching(router.equals("ghmm"))
        .build();

    Map<Type, Parameters> params = new HashMap<>();

    if (mot.contains("0") || mot.contains("tram") || mot.contains("all")) {
      params.put(Type.TRAM, railProfile);
    }

    if (mot.contains("1") || mot.contains("subway") || mot.contains("all")) {
      params.put(Type.SUBWAY, railProfile);
    }

    if (mot.contains("2") || mot.contains("rail") || mot.contains("all")) {
      params.put(Type.RAIL, railProfile);
    }

    if (mot.contains("3") || mot.contains("bus") || mot.contains("all")) {
      params.put(Type.BUS, Parameters.builder()
          .profile("bus_fastest")
          .sigma(sigma)
          .candidateSearchRadius(sigma)
          .beta(beta)
          .useGraphHopperMapMatching(router.equals("ghmm"))
          .build());
    }

    return params;
  }

}
