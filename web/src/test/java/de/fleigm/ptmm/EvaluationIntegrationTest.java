package de.fleigm.ptmm;

import de.fleigm.ptmm.http.eval.CreateEvaluationRequest;
import de.fleigm.ptmm.http.eval.EvaluationProcess;
import de.fleigm.ptmm.http.eval.EvaluationService;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class EvaluationIntegrationTest {

  @Inject
  EvaluationService evaluationService;

  @Test
  void asd() {
    String resourceAsStream = getClass().getClassLoader().getResource("test_feed.zip").getFile();
    given()
        .multiPart("feed", new File(resourceAsStream))
        .multiPart("name", "test")
        .when()
        .post("eval")
        .then()
        .statusCode(200);
  }

  @Test
  void qwe() throws IOException, ExecutionException, InterruptedException {
    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("test")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .build();

    CompletableFuture<EvaluationProcess> evaluation = evaluationService.createEvaluation(request);

    EvaluationProcess evaluationProcess = evaluation.get();

    assertTrue(evaluation.isDone());
    assertFalse(evaluation.isCompletedExceptionally());

  }
}
