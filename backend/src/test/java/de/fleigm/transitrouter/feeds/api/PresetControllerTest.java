package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.presets.GenerateFeedRequest;
import de.fleigm.transitrouter.presets.Preset;
import de.fleigm.transitrouter.presets.PresetRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PresetControllerTest {

  @ConfigProperty(name = "app.storage")
  Path appStorage;

  @Inject
  PresetRepository presets;

  @Inject
  GeneratedFeedRepository generatedFeeds;

  @Test
  void can_upload_gtfs_feed_as_preset() {
    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();
    Response response = given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "can_upload_gtfs_feed_as_preset")
        .when()
        .post("presets");

    response.then().statusCode(201);

    Preset preset = response.as(Preset.class);

    assertEquals("can_upload_gtfs_feed_as_preset", preset.getName());

    List<Preset> all = presets.all();

    assertTrue(presets.find(preset.getId()).isPresent());

    assertTrue(Files.exists(preset.getFileStoragePath().resolve("gtfs.zip")));
    assertTrue(Files.exists(preset.getFileStoragePath().resolve("gtfs")));
  }

  @Test
  void can_get_preset() {
    Preset preset = Preset.builder()
        .name("some preset")
        .build();
    presets.save(preset);

    Response response = get("presets/" + preset.getId());
    response.then().statusCode(200);

    Preset presetFromRequest = response.as(Preset.class);
    assertEquals(preset.getId(), presetFromRequest.getId());
  }

  @Test
  void can_delete_preset() {
    Preset preset = Preset.builder()
        .name("some preset")
        .build();
    presets.save(preset);

    delete("presets/" + preset.getId())
        .then()
        .statusCode(204);

    assertFalse(Files.exists(preset.getFileStoragePath()));
    assertFalse(presets.find(preset.getId()).isPresent());
  }

  @Test
  void generate_feed_from_preset() {
    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();
    Preset preset = given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "can_upload_gtfs_feed_as_preset")
        .when()
        .post("presets")
        .then()
        .statusCode(201)
        .extract()
        .as(Preset.class);

    GenerateFeedRequest request = new GenerateFeedRequest(
        "test",
        Map.of(Type.BUS.toString(), Parameters.defaultParameters()),
        false);

    GeneratedFeed generatedFeed = given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("presets/" + preset.getId() + "/generated-feeds")
        .then()
        .statusCode(200)
        .extract()
        .as(GeneratedFeed.class);

    List<GeneratedFeed> generatedFeeds = presets.generatedFeedsFromPreset(preset);
    assertEquals(1, generatedFeeds.size());
    assertTrue(generatedFeeds.contains(generatedFeed));
  }

  @Test
  void get_generate_feeds_from_preset() {
    given().when().get("presets/123/generated-feeds").then().statusCode(404);

    Preset preset = Preset.builder()
        .name("some preset")
        .build();
    presets.save(preset);

    GeneratedFeed generatedFeed = GeneratedFeed.builder()
        .name("test")
        .preset(preset.getId())
        .build();
    generatedFeeds.save(generatedFeed);

    when()
        .get("presets/" + preset.getId() + "/generated-feeds")
        .then()
        .statusCode(200)
        .body("id", hasItem(generatedFeed.getId().toString()));
  }

  @Test
  void download_preset() throws IOException {
    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();
    Preset preset = given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "can_upload_gtfs_feed_as_preset")
        .when()
        .post("presets")
        .then()
        .statusCode(201)
        .extract()
        .as(Preset.class);

    InputStream inputStream = given()
        .when()
        .get("presets/" + preset.getId() + "/download")
        .then()
        .statusCode(200)
        .extract()
        .asInputStream();

    ZipArchiveInputStream archive = new ZipArchiveInputStream(inputStream);
    List<ZipArchiveEntry> entries = new ArrayList<>();
    ZipArchiveEntry entry;
    while ((entry = archive.getNextZipEntry()) != null) {
      entries.add(entry);
    }

    List<String> fileNames = entries.stream()
        .map(ZipArchiveEntry::getName)
        .collect(Collectors.toList());

    assertTrue(fileNames.contains("gtfs.zip"));
    assertTrue(fileNames.contains("entity.json"));
  }

  @Test
  void download_not_found() {
      given().when().get("presets/123/download").then().statusCode(404);
  }
}