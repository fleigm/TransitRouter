package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.feeds.evaluation.Evaluation;
import de.fleigm.transitrouter.presets.Preset;
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
public class FeedGenerationServiceTest {

  @Inject
  FeedGenerationService feedGenerationService;

  @Test
  void happy_path() throws IOException, ExecutionException, InterruptedException {
    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name("happy_path")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("bus_shortest")
        .withEvaluation(true)
        .build();

    FeedGenerationResponse evaluation = feedGenerationService.create(request);

    evaluation.process().get();

    GeneratedFeed info = evaluation.generatedFeed();

    assertTrue(evaluation.process().isDone());
    assertFalse(evaluation.process().isCompletedExceptionally());

    assertEquals(Status.FINISHED, info.getStatus());

    assertTrue(Files.isDirectory(info.getFileStoragePath()));
    assertTrue(Files.isDirectory(info.getFileStoragePath().resolve(GeneratedFeed.ORIGINAL_GTFS_FOLDER)));
    assertTrue(Files.isDirectory(info.getFileStoragePath().resolve(GeneratedFeed.GENERATED_GTFS_FOLDER)));
    assertTrue(Files.exists(info.getFileStoragePath().resolve(GeneratedFeed.ORIGINAL_GTFS_FEED)));
    assertTrue(Files.exists(info.getFileStoragePath().resolve(GeneratedFeed.GENERATED_GTFS_FEED)));
    assertTrue(Files.exists(info.getFileStoragePath().resolve(Evaluation.SHAPEVL_REPORT)));
  }

  @Test
  void handle_failure() throws IOException, ExecutionException, InterruptedException {
    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name("test_handle_failure")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("invalid_profile")
        .build();

    FeedGenerationResponse evaluation = feedGenerationService.create(request);

    evaluation.process().get();

    GeneratedFeed info = evaluation.generatedFeed();

    assertEquals(Status.FAILED, info.getStatus());
    assertTrue(info.getErrors().stream().anyMatch(error -> error.getCode().equals("process.failed")));
  }

  @Test
  void abort_and_remove_folder_if_gtfs_feed_is_invalid() throws IOException {
    File invalidFeed = new File(getClass().getClassLoader().getResource("invalid_feed.zip").getFile());

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name("abort_and_remove_folder_if_gtfs_feed_is_invalid")
        .gtfsFeed(FileUtils.openInputStream(invalidFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("invalid_profile")
        .build();

    assertThrows(IllegalArgumentException.class, () -> feedGenerationService.create(request));

    // TODO: find a way to test if the folder was deleted. We do not know the folder name / id
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

    FeedGenerationResponse evaluation = feedGenerationService
        .createFromPreset(preset, "test name", Parameters.defaultParameters(), true);

    evaluation.process().get();

    GeneratedFeed info = evaluation.generatedFeed();

    assertTrue(evaluation.process().isDone());
    assertFalse(evaluation.process().isCompletedExceptionally());

    assertEquals(Status.FINISHED, info.getStatus());

    assertTrue(Files.isDirectory(info.getFileStoragePath()));
    assertEquals(preset.getFileStoragePath().resolve("gtfs.zip"), info.getOriginalFeed().getPath());
    assertEquals(preset.getFileStoragePath().resolve("gtfs"), info.getOriginalFeed().getFolder());
    assertTrue(Files.exists(info.getFileStoragePath().resolve(GeneratedFeed.GENERATED_GTFS_FEED)));
    assertTrue(Files.exists(info.getFileStoragePath().resolve(Evaluation.SHAPEVL_REPORT)));
  }
}
