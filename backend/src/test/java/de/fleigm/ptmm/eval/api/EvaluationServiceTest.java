package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.Status;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("bus_shortest")
        .build();

    EvaluationResponse evaluation = evaluationService.createEvaluation(request);

    evaluation.process().get();

    GeneratedFeedInfo info = evaluation.info();

    assertTrue(evaluation.process().isDone());
    assertFalse(evaluation.process().isCompletedExceptionally());

    assertEquals(Status.FINISHED, info.getStatus());

    assertTrue(Files.isDirectory(info.getPath()));
    assertTrue(Files.isDirectory(info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FOLDER)));
    assertTrue(Files.isDirectory(info.getPath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FOLDER)));
    assertTrue(Files.exists(info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED)));
    assertTrue(Files.exists(info.getPath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FEED)));
    assertTrue(Files.exists(info.getPath().resolve(EvaluationExtension.SHAPEVL_REPORT)));
    assertTrue(Files.exists(info.getPath().resolve(Evaluation.INFO_FILE)));
  }

  @Test
  void handle_failure() throws IOException, ExecutionException, InterruptedException {
    deleteFolder("test_handle_failure");

    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("test_handle_failure")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("invalid_profile")
        .build();

    EvaluationResponse evaluation = evaluationService.createEvaluation(request);

    evaluation.process().get();

    GeneratedFeedInfo info = evaluation.info();

    assertEquals(Status.FAILED, info.getStatus());
    assertTrue(info.getErrors().stream().anyMatch(error -> error.getCode().equals("process.failed")));
  }

  @Test
  void abort_and_remove_folder_if_gtfs_feed_is_invalid() throws IOException {
    File invalidFeed = new File(getClass().getClassLoader().getResource("invalid_feed.zip").getFile());

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("abort_and_remove_folder_if_gtfs_feed_is_invalid")
        .gtfsFeed(FileUtils.openInputStream(invalidFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("invalid_profile")
        .build();

    assertThrows(IllegalArgumentException.class, () -> evaluationService.createEvaluation(request));

    assertFalse(Files.exists(Path.of(evaluationFolder, "abort_and_remove_folder_if_gtfs_feed_is_invalid")));


  }
}
