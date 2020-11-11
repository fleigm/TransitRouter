package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Status;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class EvaluationServiceTest {

  @Inject
  EvaluationService evaluationService;

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  //@BeforeEach
  void cleanUp() throws IOException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, "happy_path").toFile());
  }

  private void deleteFolder(String name) {
    try {
      FileUtils.deleteDirectory(Paths.get(evaluationFolder, name).toFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }



  @Test
  void happy_path() throws IOException, ExecutionException, InterruptedException {
    deleteFolder("happy_path");

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

    CompletableFuture<Info> evaluation = evaluationService.createEvaluation(request);

    Info info = evaluation.get();

    assertTrue(evaluation.isDone());
    assertFalse(evaluation.isCompletedExceptionally());

    assertEquals(Status.FINISHED, info.getStatus());

    assertTrue(Files.isDirectory(info.fullPath(evaluationFolder)));
    assertTrue(Files.isDirectory(info.fullPath(evaluationFolder).resolve(Evaluation.ORIGINAL_GTFS_FOLDER)));
    assertTrue(Files.isDirectory(info.fullPath(evaluationFolder).resolve(Evaluation.GENERATED_GTFS_FOLDER)));
    assertTrue(Files.exists(info.fullPath(evaluationFolder).resolve(Evaluation.ORIGINAL_GTFS_FEED)));
    assertTrue(Files.exists(info.fullPath(evaluationFolder).resolve(Evaluation.GENERATED_GTFS_FEED)));
    assertTrue(Files.exists(info.fullPath(evaluationFolder).resolve(Evaluation.GTFS_FULL_REPORT)));
    assertTrue(Files.exists(info.fullPath(evaluationFolder).resolve(Evaluation.SHAPEVL_OUTPUT)));
    assertTrue(Files.exists(info.fullPath(evaluationFolder).resolve(Evaluation.INFO_FILE)));
  }

  @Test
  void handle_failure() throws IOException, ExecutionException, InterruptedException {
    deleteFolder("test_handle_failure");

    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("test_handle_failure")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .alpha(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .uTurnDistancePenalty(1500.0)
        .profile("invalid_profile")
        .build();

    CompletableFuture<Info> evaluation = evaluationService.createEvaluation(request);

    Info info = evaluation.get();

    assertEquals(Status.FAILED, info.getStatus());
    assertNotNull(info.getExtension("error.message"));
    assertNotNull(info.getExtension("error.stackTrace"));
    assertNotNull(info.getExtension("error.rootCause"));
  }
}
