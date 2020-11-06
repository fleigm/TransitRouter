package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.util.StopWatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

@Slf4j
@Dependent
public class EvaluateGtfsFeed implements Consumer<Info> {

  private final String evaluationToolPath;
  private final String evaluationFolder;

  public EvaluateGtfsFeed(@ConfigProperty(name = "evaluation.tool") String evaluationToolPath,
                          @ConfigProperty(name = "evaluation.folder") String evaluationFolder) {
    this.evaluationToolPath = evaluationToolPath;
    this.evaluationFolder = evaluationFolder;
  }

  @SneakyThrows
  @Override
  public void accept(Info info) {
    Path folder = info.fullPath(evaluationFolder);
    Path original = folder.resolve(Evaluation.ORIGINAL_GTFS_FOLDER);
    Path generated = folder.resolve(Evaluation.GENERATED_GTFS_FOLDER);
    String command = String.format("%s -m 3 -f %s -g %s %s",
        evaluationToolPath,
        folder,
        original,
        generated);

    StopWatch stopWatch = StopWatch.createAndStart();

    Process process = Runtime.getRuntime().exec(command);

    try (InputStream evalOutput = process.getInputStream()) {
      FileUtils.copyInputStreamToFile(evalOutput, folder.resolve(Evaluation.SHAPEVL_OUTPUT).toFile());
    }

    stopWatch.stop();

    info.addStatistic("executionTime.evaluation", stopWatch.getMillis());
  }
}
