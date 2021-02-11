package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.feeds.evaluation.Evaluation;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DownloadControllerTest {

  @Inject
  FeedGenerationService feedGenerationService;

  @Inject
  GeneratedFeedRepository generatedFeedRepository;


  private GeneratedFeed info;

  @BeforeEach
  void setup() throws IOException, ExecutionException, InterruptedException {
    if (info != null) {
      return;
    }

    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name("download_test")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("bus_shortest")
        .withEvaluation(true)
        .build();

    FeedGenerationResponse evaluation = feedGenerationService.create(request);

    // wait until evaluation process is finished
    evaluation.process().get();

    info = evaluation.generatedFeed();
  }


  @Test
  void can_download_generated_gtfs_feed() {
    given().when()
        .get("feeds/" + info.getId() + "/download/generated")
        .then()
        .statusCode(200)
        .contentType(ContentType.BINARY);
  }

  @Test
  void cannot_download_generated_feed_if_not_finished() {
    GeneratedFeed pending = GeneratedFeed.builder()
        .name("cannot_download_generated_feed_if_not_finished")
        .status(Status.PENDING)
        .build();

    GeneratedFeed failed = GeneratedFeed.builder()
        .name("cannot_download_generated_feed_if_not_finished")
        .status(Status.FAILED)
        .build();

    generatedFeedRepository.save(pending);
    generatedFeedRepository.save(failed);

    given().when()
        .get("feeds/" + pending.getId() + "/download/generated")
        .then()
        .statusCode(404);

    given().when()
        .get("feeds/" + failed.getId() + "/download/generated")
        .then()
        .statusCode(404);
  }

  @Test
  void can_download_all_files() throws IOException {
    Response response = given().when().get("feeds/" + info.getId() + "/download");

    response.then()
        .statusCode(200)
        .contentType(ContentType.BINARY);

    ZipArchiveInputStream archive = new ZipArchiveInputStream(response.asInputStream());
    List<ZipArchiveEntry> entries = new ArrayList<>();
    ZipArchiveEntry entry;
    while ((entry = archive.getNextZipEntry()) != null) {
      entries.add(entry);
    }

    List<String> fileNames = entries.stream()
        .map(ZipArchiveEntry::getName)
        .collect(Collectors.toList());

    assertTrue(fileNames.contains(GeneratedFeed.GENERATED_GTFS_FEED));
    assertTrue(fileNames.contains(GeneratedFeed.ORIGINAL_GTFS_FEED));
    assertTrue(fileNames.contains("entity.json"));
    assertTrue(fileNames.contains(Evaluation.SHAPEVL_REPORT));
  }

  @Test
  void return_404_if_evaluation_does_not_exist() {
    UUID id = UUID.randomUUID();
    given().when()
        .get("feeds/" + id + "/download")
        .then()
        .statusCode(404);

    given().when()
        .get("feeds/" + id + "/download/generated")
        .then()
        .statusCode(404);
  }
}
