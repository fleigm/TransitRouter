package de.fleigm.ptmm.feeds.api;

import de.fleigm.ptmm.presets.Preset;
import de.fleigm.ptmm.presets.PresetRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PresetControllerTest {

  @ConfigProperty(name = "app.storage")
  Path appStorage;

  @Inject
  PresetRepository presets;

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
}