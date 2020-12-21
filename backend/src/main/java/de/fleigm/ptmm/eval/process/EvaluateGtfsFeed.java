package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.util.StopWatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@Slf4j
public class EvaluateGtfsFeed implements Consumer<Info> {

  private final String evaluationToolPath;

  public EvaluateGtfsFeed(String evaluationToolPath) {
    this.evaluationToolPath = evaluationToolPath;
  }

  @SneakyThrows
  @Override
  public void accept(Info info) {
    log.info("Start evaluation step.");

    Path folder = info.getPath();
    Path original = folder.resolve(Evaluation.ORIGINAL_GTFS_FOLDER);
    Path generated = folder.resolve(Evaluation.GENERATED_GTFS_FOLDER);

    String[] command = {
        evaluationToolPath,
        "-m", "3",
        "-f", folder.toString(),
        "-g", original.toString(),
        generated.toString()
    };

    File outputFile = Files.createFile(folder.resolve(Evaluation.SHAPEVL_OUTPUT)).toFile();

    StopWatch stopWatch = StopWatch.createAndStart();

    Process process = new ProcessBuilder(command)
        .redirectInput(outputFile)
        .redirectError(outputFile)
        .start();

    process.waitFor();

    if (process.exitValue() != 0) {
      throw new RuntimeException("Evaluation tool failed.");
    }

    stopWatch.stop();

    info.addStatistic("executionTime.evaluation", stopWatch.getMillis());

    log.info("Finish evaluation step. Took {}s", stopWatch.getSeconds());
  }
}
