package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.Parameters;
import de.fleigm.ptmm.eval.Status;
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
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class EvaluationEndpointTest {

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  @Inject
  EvaluationRepository evaluationRepository;

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
        .post("eval");

    response.then().statusCode(201);

    GeneratedFeedInfo info = response.as(GeneratedFeedInfo.class);

    assertEquals("endpoint_happy_path", info.getName());

    assertTrue(Files.exists(info.getPath()));
  }

  @ParameterizedTest
  @EnumSource(value = Status.class, names = {"FINISHED", "FAILED"})
  void can_delete_evaluation(Status status) throws IOException {
    String evaluationName = "can_delete_evaluation";

    GeneratedFeedInfo info = GeneratedFeedInfo.builder()
        .name(evaluationName)
        .createdAt(LocalDateTime.now())
        .parameters(Parameters.defaultParameters())
        .status(status)
        .build();

    evaluationRepository.save(info);

    given().when()
        .delete("eval/" + info.getId())
        .then()
        .statusCode(204);

    assertTrue(evaluationRepository.find(info.getId()).isEmpty());
    assertFalse(Files.exists(info.getPath()));
  }

  @Test
  void cannot_delete_pending_evaluation() throws IOException {
    String evaluationName = "cannot_delete_pending_evaluation";

    GeneratedFeedInfo info = GeneratedFeedInfo.builder()
        .name(evaluationName)
        .createdAt(LocalDateTime.now())
        .parameters(Parameters.defaultParameters())
        .status(Status.PENDING)
        .build();

    evaluationRepository.save(info);

    given().when()
        .delete("eval/" + info.getId())
        .then()
        .statusCode(409);

    assertTrue(evaluationRepository.find(info.getId()).isPresent());
    assertTrue(Files.exists(info.getPath()));
  }
}
