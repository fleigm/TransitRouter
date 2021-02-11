package de.fleigm.transitrouter.feeds.evaluation;

import de.fleigm.transitrouter.util.Unzip;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ShapevlTest {

  @ConfigProperty(name = "app.evaluation-tool")
  Path shapevlCommandPath;

  Path tempDirectory = Files.createTempDirectory("shapevl-test");

  private Shapevl shapevl;

  ShapevlTest() throws IOException {
  }

  @BeforeEach
  void beforeEach() {
    shapevl = new Shapevl(shapevlCommandPath);
  }

  @Test
  void run() throws IOException {
    Path feedZip = Path.of(getClass().getClassLoader().getResource("test_feed.zip").getPath());
    Path feedPath = tempDirectory.resolve("run");
    Unzip.apply(feedZip, feedPath);

    Shapevl.Result result = shapevl.run(feedPath, feedPath, feedPath);

    assertFalse(result.hasFailed());
    assertFalse(result.getMessage().isBlank());
    assertTrue(Files.exists(feedPath.resolve("run.fullreport.tsv")));
  }

  @Test
  void show_error_message() {
    Path invalidPath = tempDirectory.resolve("show_error");
    Shapevl.Result result = shapevl.run(invalidPath, invalidPath, invalidPath);

    assertTrue(result.hasFailed());
    assertFalse(result.getMessage().isBlank());
    assertFalse(Files.exists(tempDirectory.resolve("show_error.fullreport.tsv")));
  }

}