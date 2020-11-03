package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.util.StopWatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.function.Function;

@Slf4j
public class EvaluateGtfsFeed implements Function<EvaluationProcess, EvaluationProcess> {

  @SneakyThrows
  @Override
  public EvaluationProcess apply(EvaluationProcess evaluationProcess) {
    String original = evaluationProcess.getPath() + Evaluation.ORIGINAL_GTFS_FOLDER;
    String generated = evaluationProcess.getPath() + Evaluation.GENERATED_GTFS_FOLDER;
    String command = String.format("%sshapevl -m 3 -f %s -g %s %s",
        evaluationProcess.getBaseFolder(),
        evaluationProcess.getPath(),
        original,
        generated);

    StopWatch stopWatch = StopWatch.createAndStart();

    Process process = Runtime.getRuntime().exec(command);

    try (InputStream evalOutput = process.getInputStream()) {
      FileUtils.copyInputStreamToFile(
          evalOutput,
          new File(evaluationProcess.getPath() + Evaluation.SHAPEVL_OUTPUT));
    }

    stopWatch.stop();

    evaluationProcess.getInfo().addStatistic("executionTime.evaluation", stopWatch.getMillis());

    return evaluationProcess;
  }
}
