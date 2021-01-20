package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.util.StopWatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@Slf4j
public class EvaluateGtfsFeed implements Consumer<GeneratedFeedInfo> {

  private final String evaluationToolPath;

  public EvaluateGtfsFeed(String evaluationToolPath) {
    this.evaluationToolPath = evaluationToolPath;
  }

  @SneakyThrows
  @Override
  public void accept(GeneratedFeedInfo info) {
    log.info("Start evaluation step.");

    Path folder = info.getPath();
    Path original = info.getOriginalFeed().resolveSibling(FilenameUtils.removeExtension(info.getOriginalFeed().toString()));
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
        .redirectErrorStream(true)
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
