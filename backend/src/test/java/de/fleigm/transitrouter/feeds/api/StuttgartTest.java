package de.fleigm.transitrouter.feeds.api;

import com.graphhopper.GraphHopper;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.routing.GraphHopperTransitRouter;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@QuarkusTest
public class StuttgartTest {

  @Inject
  FeedGenerationService feedGenerationService;

  @ConfigProperty(name = "user.home")
  String homeDir;

  @ConfigProperty(name = "app.evaluation-tool")
  Path evaluationTool;

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  GraphHopper graphHopper;

  @Test
  void run_evaluation() throws IOException, ExecutionException, InterruptedException {
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart.zip").toFile();

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name("st_complete")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(10.0)
        .candidateSearchRadius(10.0)
        .beta(1.0)
        .profile("bus_fastest_turn")
        .build();

    FeedGenerationService feedGenerationService = new FeedGenerationService();
    feedGenerationService.generatedFeedRepository = generatedFeedRepository;
    feedGenerationService.evaluationTool = evaluationTool;
    feedGenerationService.transitRouterFactory = parameters -> new GraphHopperTransitRouter(graphHopper, parameters);

    FeedGenerationResponse result = feedGenerationService.create(request);

    result.process().get();

    assertTrue(result.process().isDone());
  }

  @ParameterizedTest
  @ValueSource(strings = {"bus_fastest", "bus_fastest_turn", "bus_shortest", "bus_shortest_turn"})
  void run_all_evaluations(String profile) throws IOException, ExecutionException, InterruptedException {
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart_bus_only.zip").toFile();

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name(profile)
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile(profile)
        .build();

    FeedGenerationResponse result = feedGenerationService.create(request);

    result.process().get();

    assertTrue(result.process().isDone());
  }

  @ParameterizedTest
  @CsvSource({
      "20, 2.0", "15, 2.0", "10, 2.0", "5, 2.0",
      "20, 1.5", "15, 1.5", "10, 1.5", "5, 1.5",
      "20, 1.0", "15, 1.0", "10, 1.0", "5, 1.0",
      "20, 0.5", "15, 0.5", "10, 0.5", "5, 0.5"})
  void run_with_different_parameters(double sigma, double beta) throws IOException, ExecutionException, InterruptedException {
    File feed = Paths.get(homeDir, "/uni/bachelor/project/files/vg_converted.zip").toFile();

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name(String.format("vg_converted_%.0f_%.1f", sigma, beta))
        .gtfsFeed(FileUtils.openInputStream(feed))
        .sigma(sigma)
        .candidateSearchRadius(sigma)
        .beta(beta)
        .profile("bus_fastest_turn")
        .build();

    FeedGenerationResponse result = feedGenerationService.create(request);

    result.process().get();

    assertTrue(result.process().isDone());
    assertFalse(result.process().isCompletedExceptionally());
  }

  @ParameterizedTest
  @ValueSource(strings = {"bus_shortest_turn", "bus_fastest_turn"})
  void ptmm(String profile) throws ExecutionException, InterruptedException, IOException {
    File feed = Paths.get(homeDir, "/uni/bachelor/project/files/paris2.zip").toFile();

    GenerateFeedRequest ptmmRequest = GenerateFeedRequest.builder()
        .name(String.format("ptmm_paris_10_1_%s", profile))
        .gtfsFeed(FileUtils.openInputStream(feed))
        .sigma(10.0)
        .candidateSearchRadius(10.0)
        .beta(1.0)
        .profile(profile)
        .build();

    GenerateFeedRequest ghRequest = GenerateFeedRequest.builder()
        .name(String.format("graphHopper_paris_10_1_%s", profile))
        .gtfsFeed(FileUtils.openInputStream(feed))
        .sigma(10.0)
        .candidateSearchRadius(10.0)
        .beta(1.0)
        .profile(profile)
        .useGraphHopperMapMatching(true)
        .build();

    FeedGenerationResponse ptmmResult = feedGenerationService.create(ptmmRequest);
    FeedGenerationResponse ghResult = feedGenerationService.create(ghRequest);

    ptmmResult.process().get();
    ghResult.process().get();

    assertTrue(ptmmResult.process().isDone());
    assertFalse(ptmmResult.process().isCompletedExceptionally());

    assertTrue(ghResult.process().isDone());
    assertFalse(ghResult.process().isCompletedExceptionally());
  }

  @Test
  void run_graphHopper_map_matching() throws IOException, ExecutionException, InterruptedException {
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart.zip").toFile();

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name("st_gh_complete")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("bus_fastest")
        .build();

    FeedGenerationService service = new FeedGenerationService();
    service.generatedFeedRepository = generatedFeedRepository;
    service.evaluationTool = evaluationTool;
    service.transitRouterFactory = parameters -> new GraphHopperTransitRouter(graphHopper, parameters);

    FeedGenerationResponse result = service.create(request);

    result.process().get();

    assertTrue(result.process().isDone());

  }
}
