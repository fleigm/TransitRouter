package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.util.StopWatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

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

    EvaluationExtension evaluation = info.getOrCreateExtension(EvaluationExtension.class, EvaluationExtension::new);

    evaluation.setStatus(Status.PENDING);

    String[] command = {
        evaluationToolPath,
        "-m", "3",
        "-f", info.getPath().toString(),
        "-g", FilenameUtils.removeExtension(info.getOriginalFeed().toString()),
        FilenameUtils.removeExtension(info.getGeneratedFeed().toString())
    };

    StopWatch stopWatch = StopWatch.createAndStart();

    Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

    evaluation.setShapevlOutput(new String(process.getInputStream().readAllBytes()));

    process.waitFor();

    if (process.exitValue() != 0) {
      evaluation.setStatus(Status.FAILED);
      throw new RuntimeException("Evaluation tool failed.");
    }

    evaluation.setReport(info.getPath().resolve(EvaluationExtension.SHAPEVL_REPORT));

    stopWatch.stop();

    info.addStatistic("executionTime.evaluation", stopWatch.getMillis());

    log.info("Finish evaluation step. Took {}s", stopWatch.getSeconds());
  }
}
