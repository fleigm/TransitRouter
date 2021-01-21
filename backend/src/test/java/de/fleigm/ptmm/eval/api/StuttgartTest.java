package de.fleigm.ptmm.eval.api;

import com.graphhopper.GraphHopper;
import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.routing.GraphHopperTransitRouter;
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
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
@QuarkusTest
public class StuttgartTest {

  @Inject
  EvaluationService evaluationService;

  @ConfigProperty(name = "user.home")
  String homeDir;

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  @ConfigProperty(name = "evaluation.tool")
  String evaluationTool;

  @Inject
  EvaluationRepository evaluationRepository;

  @Inject
  GraphHopper graphHopper;

  @Test
  void run_evaluation() throws IOException, ExecutionException, InterruptedException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, "st_complete").toFile());
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart.zip").toFile();

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("st_complete")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(10.0)
        .candidateSearchRadius(10.0)
        .beta(1.0)
        .profile("bus_fastest_turn")
        .build();

    EvaluationService evaluationService = new EvaluationService();
    evaluationService.evaluationRepository = evaluationRepository;
    evaluationService.evaluationTool = evaluationTool;
    evaluationService.evaluationFolder = evaluationFolder;
    evaluationService.transitRouterFactory = parameters -> new GraphHopperTransitRouter(graphHopper, parameters);

    EvaluationResponse result = evaluationService.createEvaluation(request);

    result.process().get();

    assertTrue(result.process().isDone());
  }

  @ParameterizedTest
  @ValueSource(strings = {"bus_fastest", "bus_fastest_turn", "bus_shortest", "bus_shortest_turn"})
  void run_all_evaluations(String profile) throws IOException, ExecutionException, InterruptedException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, profile).toFile());
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart_bus_only.zip").toFile();

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name(profile)
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile(profile)
        .build();

    EvaluationResponse result = evaluationService.createEvaluation(request);

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

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name(String.format("vg_converted_%.0f_%.1f", sigma, beta))
        .gtfsFeed(FileUtils.openInputStream(feed))
        .sigma(sigma)
        .candidateSearchRadius(sigma)
        .beta(beta)
        .profile("bus_fastest_turn")
        .build();

    EvaluationService service = new EvaluationService();
    service.evaluationRepository = evaluationRepository;
    service.evaluationTool = evaluationTool;
    service.evaluationFolder = evaluationFolder;
    service.transitRouterFactory = parameters -> new GraphHopperTransitRouter(graphHopper, parameters);

    EvaluationResponse result = service.createEvaluation(request);

    result.process().get();

    assertTrue(result.process().isDone());
    assertFalse(result.process().isCompletedExceptionally());
  }

  @ParameterizedTest
  @ValueSource(strings = {"bus_shortest_turn", "bus_fastest_turn"})
  void ptmm(String profile) throws ExecutionException, InterruptedException, IOException {
    File feed = Paths.get(homeDir, "/uni/bachelor/project/files/paris2.zip").toFile();

    CreateEvaluationRequest ptmmRequest = CreateEvaluationRequest.builder()
        .name(String.format("ptmm_paris_10_1_%s", profile))
        .gtfsFeed(FileUtils.openInputStream(feed))
        .sigma(10.0)
        .candidateSearchRadius(10.0)
        .beta(1.0)
        .profile(profile)
        .build();

    CreateEvaluationRequest ghRequest = CreateEvaluationRequest.builder()
        .name(String.format("graphHopper_paris_10_1_%s", profile))
        .gtfsFeed(FileUtils.openInputStream(feed))
        .sigma(10.0)
        .candidateSearchRadius(10.0)
        .beta(1.0)
        .profile(profile)
        .build();

    EvaluationService service = new EvaluationService();
    service.evaluationRepository = evaluationRepository;
    service.evaluationTool = evaluationTool;
    service.evaluationFolder = evaluationFolder;
    service.transitRouterFactory = parameters -> new GraphHopperTransitRouter(graphHopper, parameters);

    EvaluationResponse ptmmResult = evaluationService.createEvaluation(ptmmRequest);
    EvaluationResponse ghResult = service.createEvaluation(ghRequest);

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

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("st_gh_complete")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("bus_fastest")
        .build();

    EvaluationService service = new EvaluationService();
    service.evaluationRepository = evaluationRepository;
    service.evaluationTool = evaluationTool;
    service.evaluationFolder = evaluationFolder;
    service.transitRouterFactory = parameters -> new GraphHopperTransitRouter(graphHopper, parameters);

    EvaluationResponse result = service.createEvaluation(request);

    result.process().get();

    assertTrue(result.process().isDone());

  }
}
