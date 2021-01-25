package de.fleigm.ptmm.data;

import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import de.fleigm.ptmm.eval.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionsTest {

  private Path path;
  private GeneratedFeedRepository repository;

  @BeforeEach
  void beforeEach() throws IOException {
    this.path = Files.createTempDirectory("repositoryTest");
    this.repository = new GeneratedFeedRepository(this.path);
  }

  @Test
  void json_serialization() {

    GeneratedFeedInfo info = GeneratedFeedInfo.builder()
        .name("test-feed")
        .build();

    EvaluationExtension extension = info.getOrCreateExtension(EvaluationExtension.class, EvaluationExtension::new);

    extension.setStatus(Status.FINISHED);
    extension.setReport(Path.of("test-report"));
    extension.setShapevlOutput("test output");
    extension.setQuickStats(new HashMap<>());
    extension.setExecutionTime(Duration.ofMillis(1000));

    repository.save(info);

    repository = new GeneratedFeedRepository(path);
    GeneratedFeedInfo reloadedInfo = repository.find(info.getId()).get();

    assertTrue(reloadedInfo.hasExtension(EvaluationExtension.class));
    EvaluationExtension ext = reloadedInfo.getExtension(EvaluationExtension.class).get();
    assertEquals(ext, extension);

  }
}