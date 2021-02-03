package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.Parameters;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.presets.Preset;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class EvaluationServiceTest {

  @Inject
  EvaluationService evaluationService;

  @Test
  void happy_path() throws IOException, ExecutionException, InterruptedException {
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

    assertTrue(Files.isDirectory(info.getFileStoragePath()));
    assertTrue(Files.isDirectory(info.getFileStoragePath().resolve(GeneratedFeedInfo.ORIGINAL_GTFS_FOLDER)));
    assertTrue(Files.isDirectory(info.getFileStoragePath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FOLDER)));
    assertTrue(Files.exists(info.getFileStoragePath().resolve(GeneratedFeedInfo.ORIGINAL_GTFS_FEED)));
    assertTrue(Files.exists(info.getFileStoragePath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FEED)));
    assertTrue(Files.exists(info.getFileStoragePath().resolve(EvaluationExtension.SHAPEVL_REPORT)));
  }

  @Test
  void handle_failure() throws IOException, ExecutionException, InterruptedException {
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

    //assertFalse(Files.exists(Path.of(evaluationFolder, "abort_and_remove_folder_if_gtfs_feed_is_invalid")));
    assertFalse(true);
  }

  @Test
  void generate_from_preset() throws ExecutionException, InterruptedException {
    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();
    Response response = given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "preset")
        .when()
        .post("presets");
    response.then().statusCode(201);
    Preset preset = response.as(Preset.class);

    EvaluationResponse evaluation = evaluationService.createFromPreset(preset, "test name", Parameters.defaultParameters());

    evaluation.process().get();

    GeneratedFeedInfo info = evaluation.info();

    assertTrue(evaluation.process().isDone());
    assertFalse(evaluation.process().isCompletedExceptionally());

    assertEquals(Status.FINISHED, info.getStatus());

    assertTrue(Files.isDirectory(info.getFileStoragePath()));
    assertEquals(preset.getFileStoragePath().resolve("gtfs.zip"), info.getOriginalFeed().getPath());
    assertEquals(preset.getFileStoragePath().resolve("gtfs"), info.getOriginalFeed().getFolder());
    assertTrue(Files.exists(info.getFileStoragePath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FEED)));
    assertTrue(Files.exists(info.getFileStoragePath().resolve(EvaluationExtension.SHAPEVL_REPORT)));
  }
}
