package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.feeds.Status;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class EvaluationEndpointTest {

  @ConfigProperty(name = "app.storage")
  Path storagePath;

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Test
  void send_evaluation_request() throws IOException {
    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();
    Response response = given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "endpoint_happy_path")
        .multiPart("profile", "bus_shortest")
        .multiPart("sigma", 25)
        .multiPart("candidateSearchRadius", 25)
        .multiPart("beta", 2.0)
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
        .parameters(Parameters.defaultParameters())
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
        .parameters(Parameters.defaultParameters())
        .status(Status.PENDING)
        .build();

    generatedFeedRepository.save(info);

    given().when()
        .delete("feeds/" + info.getId())
        .then()
        .statusCode(409);

    assertTrue(generatedFeedRepository.find(info.getId()).isPresent());
    //assertTrue(Files.exists(info.getFileStoragePath()));
  }
}
