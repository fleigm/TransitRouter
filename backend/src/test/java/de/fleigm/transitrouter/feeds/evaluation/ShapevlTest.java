package de.fleigm.transitrouter.feeds.evaluation;

import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.util.Unzip;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

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
  void command_builder() {
    Shapevl shapevl = new Shapevl(Path.of("shapevl"));

    Path feed = Path.of("feed");
    Path groundTruth = Path.of("groundTruth");
    Path folder = Path.of("folder");

    assertArrayEquals(
        new String[]{"shapevl", "-f", "folder", "-g", "groundTruth", "feed"},
        shapevl.buildCommand(feed, groundTruth, folder, Type.TRAM, Type.SUBWAY, Type.RAIL, Type.BUS));
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