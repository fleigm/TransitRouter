package de.fleigm.transitrouter.feeds.evaluation;

import de.fleigm.transitrouter.gtfs.Type;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Java wrapper for the evaluation tool shapevl.
 */
public class Shapevl {
  private final Path shapevlCommandPath;

  public Shapevl(@ConfigProperty(name = "app.evaluation-tool") Path shapevlCommandPath) {
    this.shapevlCommandPath = shapevlCommandPath;
  }

  public Result run(Path feed, Path groundTruth, Path reportFolder) {
    return run(feed, groundTruth, reportFolder, Type.TRAM, Type.SUBWAY, Type.RAIL, Type.BUS);
  }

  /**
   * Runs shapevl with the given parameters.
   * This blocks the current thread.
   *
   * @param feed path to generated GTFS feed (folder)
   * @param groundTruth path to original GTFS feed (folder)
   * @param reportFolder path where the report file should be stored.
   * @return result of shapevl execution.
   */
  public Result run(Path feed, Path groundTruth, Path reportFolder, Type ...types) {
    String[] command = buildCommand(feed, groundTruth, reportFolder, types);

    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

      process.waitFor();

      String output = new String(process.getInputStream().readAllBytes());

      return new Result(String.join(" ", command), output, process.exitValue() != 0);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  protected String[] buildCommand(Path feed, Path groundTruth, Path reportFolder, Type ...types) {
    String mot = Arrays.stream(types)
        .map(type -> String.valueOf(type.value()))
        .collect(Collectors.joining(","));

    return new String[]{
        shapevlCommandPath.toString(),
        //"-m", mot,
        "-f", reportFolder.toString(),
        "-g", groundTruth.toString(),
        feed.toString()
    };
  }

  /**
   * Contains the cmd output of shapevl
   */
  @ToString
  @EqualsAndHashCode
  public static class Result {
    private final String command;
    private final String message;
    private final boolean failed;

    public Result(String command, String message, boolean failed) {
      this.command = command;
      this.message = message;
      this.failed = failed;
    }

    public String getCommand() {
      return command;
    }

    public String getMessage() {
      return message;
    }

    public boolean hasFailed() {
      return failed;
    }
  }
}
