package de.fleigm.ptmm;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.http.eval.CreateEvaluationRequest;
import de.fleigm.ptmm.http.eval.EvaluationProcess;
import de.fleigm.ptmm.http.eval.EvaluationService;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class EvaluationIntegrationTest {

  @Inject
  EvaluationService evaluationService;

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  @BeforeEach
  void cleanUp() throws IOException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, "test").toFile());
  }

  @Test
  void endpoint_happy_path() {
    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();
    given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "endpoint_happy_path")
        .multiPart("profile", "bus_custom_shortest")
        .multiPart("alpha", 25)
        .multiPart("candidateSearchRadius", 25)
        .multiPart("beta", 2.0)
        .multiPart("uTurnDistancePenalty", 1500)
        .when()
        .post("eval")
        .then()
        .statusCode(200);
  }

  @Test
  void happy_path() throws IOException, ExecutionException, InterruptedException {
    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("happy_path")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .alpha(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .uTurnDistancePenalty(1500.0)
        .profile("bus_custom_shortest")
        .build();

    CompletableFuture<EvaluationProcess> evaluation = evaluationService.createEvaluation(request);

    EvaluationProcess evaluationProcess = evaluation.get();

    assertTrue(evaluation.isDone());
    assertFalse(evaluation.isCompletedExceptionally());

    assertTrue(Files.isDirectory(Paths.get(evaluationProcess.getPath())));
    assertTrue(Files.isDirectory(Paths.get(evaluationProcess.getPath() + Evaluation.ORIGINAL_GTFS_FOLDER)));
    assertTrue(Files.isDirectory(Paths.get(evaluationProcess.getPath() + Evaluation.GENERATED_GTFS_FOLDER)));
    assertTrue(Files.exists(Paths.get(evaluationProcess.getPath() + Evaluation.ORIGINAL_GTFS_FEED)));
    assertTrue(Files.exists(Paths.get(evaluationProcess.getPath() + Evaluation.GENERATED_GTFS_FEED)));
    assertTrue(Files.exists(Paths.get(evaluationProcess.getPath() + Evaluation.GTFS_FULL_REPORT)));
    assertTrue(Files.exists(Paths.get(evaluationProcess.getPath() + Evaluation.SHAPEVL_OUTPUT)));
    assertTrue(Files.exists(Paths.get(evaluationProcess.getPath() + Evaluation.INFO_FILE)));
  }
}
