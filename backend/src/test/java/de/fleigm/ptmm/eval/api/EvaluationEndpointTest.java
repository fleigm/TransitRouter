package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Parameters;
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
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class EvaluationEndpointTest {

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  @Inject
  EvaluationRepository evaluationRepository;

  @Test
  void send_evaluation_request() throws IOException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, "endpoint_happy_path").toFile());

    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();
    given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "endpoint_happy_path")
        .multiPart("profile", "bus_custom_shortest")
        .multiPart("alpha", 25)
        .multiPart("candidateSearchRadius", 25)
        .multiPart("beta", 2.0)
        .multiPart("uTurnDistancePenalty", 1500)
        .when()
        .post("eval")
        .then()
        .statusCode(200);

    assertTrue(Files.exists(Paths.get(evaluationFolder, "endpoint_happy_path")));
    assertTrue(evaluationRepository.find("endpoint_happy_path").isPresent());
  }

  @Test
  void can_delete_evaluation() throws IOException {
    String evaluationName = "can_delete_evaluation";

    FileUtils.deleteDirectory(Paths.get(evaluationFolder, evaluationName).toFile());

    Info info = Info.builder()
        .name(evaluationName)
        .createdAt(LocalDateTime.now())
        .parameters(Parameters.defaultParameters())
        .status(Status.FINISHED)
        .build();

    evaluationRepository.save(info);

    given().when()
        .delete("eval/" + evaluationName)
        .then()
        .statusCode(204);

    assertTrue(evaluationRepository.find(evaluationName).isEmpty());
    assertFalse(Files.exists(Paths.get(evaluationFolder, evaluationName)));
  }

  @Test
  void cannot_delete_pending_evaluation() throws IOException {
    String evaluationName = "cannot_delete_pending_evaluation";

    FileUtils.deleteDirectory(Paths.get(evaluationFolder, evaluationName).toFile());

    Info info = Info.builder()
        .name(evaluationName)
        .createdAt(LocalDateTime.now())
        .parameters(Parameters.defaultParameters())
        .status(Status.PENDING)
        .build();

    evaluationRepository.save(info);

    given().when()
        .delete("eval/" + evaluationName)
        .then()
        .statusCode(409);

    assertTrue(evaluationRepository.find(evaluationName).isPresent());
    assertTrue(Files.exists(Paths.get(evaluationFolder, evaluationName)));
  }
}
