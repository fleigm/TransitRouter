package de.fleigm.transitrouter.feeds.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.gtfs.FeedDetails;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.presets.Preset;
import de.fleigm.transitrouter.presets.PresetRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.response.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class GeneratedFeedControllerTest {

  @ConfigProperty(name = "app.storage")
  Path storagePath;

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  PresetRepository presetRepository;

  @Inject
  ObjectMapper objectMapper;

  @BeforeEach
  void beforeEach() {
    RestAssured.config = RestAssured.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory((type, s) -> objectMapper));
  }

  @Test
  void send_evaluation_request() throws IOException {
    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();

    String params = objectMapper.writeValueAsString(Map.of("BUS", Parameters.defaultParameters()));

    Response response = given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "endpoint_happy_path")
        .multiPart("parameters", params)
        .when()
        .post("feeds");

    response.then().statusCode(201);

    GeneratedFeed info = response.as(GeneratedFeed.class);

    assertEquals("endpoint_happy_path", info.getName());

    assertTrue(Files.exists(info.getFileStoragePath()));
  }

  @ParameterizedTest
  @EnumSource(value = Status.class, names = {"FINISHED", "FAILED"})
  void can_delete_evaluation(Status status) throws IOException {
    String evaluationName = "can_delete_evaluation";

    GeneratedFeed info = GeneratedFeed.builder()
        .name(evaluationName)
        .parameters(Map.of(Type.BUS, Parameters.defaultParameters()))
        .status(status)
        .build();

    generatedFeedRepository.save(info);

    given().when()
        .delete("feeds/" + info.getId())
        .then()
        .statusCode(204);

    assertTrue(generatedFeedRepository.find(info.getId()).isEmpty());
    assertFalse(Files.exists(info.getFileStoragePath()));
  }

  @Test
  void cannot_delete_pending_evaluation() throws IOException {
    String evaluationName = "cannot_delete_pending_evaluation";

    GeneratedFeed info = GeneratedFeed.builder()
        .name(evaluationName)
        .parameters(Map.of(Type.BUS, Parameters.defaultParameters()))
        .status(Status.PENDING)
        .build();

    generatedFeedRepository.save(info);

    given().when()
        .delete("feeds/" + info.getId())
        .then()
        .statusCode(409);

    assertTrue(generatedFeedRepository.find(info.getId()).isPresent());
    assertTrue(Files.exists(info.getFileStoragePath()));
  }

  @Test
  void get_generated_feed() {
    GeneratedFeed info = GeneratedFeed.builder()
        .name("some random feed")
        .parameters(Map.of(Type.BUS, Parameters.defaultParameters()))
        .status(Status.PENDING)
        .build();

    generatedFeedRepository.save(info);

    GeneratedFeed generatedFeed = given().when()
        .get("feeds/" + info.getId())
        .then()
        .statusCode(200)
        .extract()
        .as(GeneratedFeed.class);

    assertEquals(generatedFeed.getId(), info.getId());
  }

  @Test
  void load_feed_info_from_preset() {
    Preset preset = Preset.builder()
        .name("some preset")
        .build();
    FeedDetails feedDetails = new FeedDetails();
    preset.addExtension(feedDetails);
    presetRepository.save(preset);

    GeneratedFeed generatedFeed = GeneratedFeed.builder()
        .name("some random feed")
        .parameters(Map.of(Type.BUS, Parameters.defaultParameters()))
        .status(Status.PENDING)
        .preset(preset.getId())
        .build();

    generatedFeedRepository.save(generatedFeed);

    GeneratedFeed feed = given().when()
        .get("feeds/" + generatedFeed.getId())
        .then()
        .statusCode(200)
        .extract()
        .as(GeneratedFeed.class);

    assertTrue(feed.hasExtension(FeedDetails.class));
  }

  @Test
  void not_found() {
      given().when()
          .get("feeds/" + UUID.randomUUID())
          .then()
          .statusCode(404);
  }
}
