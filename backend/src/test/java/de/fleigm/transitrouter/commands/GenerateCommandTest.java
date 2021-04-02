package de.fleigm.transitrouter.commands;

import de.fleigm.transitrouter.gtfs.Type;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class GenerateCommandTest {

  @ConfigProperty(name = "app.gh.osm")
  Path osmFile;

  private static GenerateCommand command;
  private static CommandLine cmd;
  private static Path directory;

  private Path gtfsFeed;

  @BeforeAll
  static void setup() throws IOException {
    command = new GenerateCommand();
    command.init();
    cmd = new CommandLine(command);
    directory = Files.createTempDirectory("generate_command_test");
    markForAutoRemoval(directory);
  }

  @BeforeEach
  void beforeEach() throws IOException {
    FileUtils.cleanDirectory(directory.toFile());
    FileUtils.deleteQuietly(command.storage.resolve("gtfs.zip").toFile());
    FileUtils.deleteQuietly(command.storage.resolve("gtfs").toFile());
    FileUtils.deleteQuietly(command.storage.resolve("generated").toFile());

    gtfsFeed = directory.resolve("gtfs.zip");
    Files.copy(
        getClass().getClassLoader().getResource("test_feed.zip").openStream(),
        gtfsFeed);

  }

  @Test
  void generate_feed() throws IOException {
    cmd.execute("-x", osmFile.toString(), gtfsFeed.toString());

    assertTrue(Files.exists(directory.resolve("gtfs.generated.zip")));

    assertTrue(command.generatedFeed.getParameters().containsKey(Type.TRAM));
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.SUBWAY));
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.RAIL));
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.BUS));
  }

  @Test
  void with_evaluation() {
    cmd.execute("-x", osmFile.toString(), "-e", gtfsFeed.toString());

    assertTrue(Files.exists(directory.resolve("gtfs.generated.zip")));
    assertTrue(Files.exists(directory.resolve("gtfs.generated.fullreport.tsv")));

    assertTrue(command.generatedFeed.getParameters().containsKey(Type.TRAM));
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.SUBWAY));
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.RAIL));
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.BUS));
  }

  @Test
  void set_mot() {
    cmd.execute("-x", osmFile.toString(), "-m", "3" ,gtfsFeed.toString());
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.BUS));
    assertEquals(1, command.generatedFeed.getParameters().size());
  }

  @Test
  void set_multiple_mot() {
    cmd.execute("-x", osmFile.toString(), "-m", "2,3" ,gtfsFeed.toString());
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.BUS));
    assertTrue(command.generatedFeed.getParameters().containsKey(Type.RAIL));
    assertEquals(2, command.generatedFeed.getParameters().size());
  }

  private static void markForAutoRemoval(Path path) {
    markForAutoRemoval(path.toFile());
  }
  private static void markForAutoRemoval(File file) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(file)));
  }

}