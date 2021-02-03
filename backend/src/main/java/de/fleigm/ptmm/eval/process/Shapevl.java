package de.fleigm.ptmm.eval.process;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Path;

public class Shapevl {
  private final Path shapevlCommandPath;

  public Shapevl(@ConfigProperty(name = "app.evaluation-tool") Path shapevlCommandPath) {
    this.shapevlCommandPath = shapevlCommandPath;
  }

  public Result run(Path feed, Path groundTruth, Path reportFolder) {
    String[] command = {
        shapevlCommandPath.toString(),
        "-m", "3",
        "-f", reportFolder.toString(),
        "-g", groundTruth.toString(),
        feed.toString()
    };

    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

      process.waitFor();

      String output = new String(process.getInputStream().readAllBytes());

      return new Result(output, process.exitValue() != 0);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @ToString
  @EqualsAndHashCode
  public static class Result {
    private final String message;
    private final boolean failed;

    public Result(String message, boolean failed) {
      this.message = message;
      this.failed = failed;
    }

    public String getMessage() {
      return message;
    }

    public boolean hasFailed() {
      return failed;
    }
  }
}
