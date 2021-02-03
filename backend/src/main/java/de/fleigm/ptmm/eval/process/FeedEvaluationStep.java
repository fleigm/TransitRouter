package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Run feed evaluation
 */
public class FeedEvaluationStep {
  private static final Logger logger = LoggerFactory.getLogger(FeedEvaluationStep.class);
  private final Shapevl shapevl;

  public FeedEvaluationStep(Path shapevlCommandPath) {
    this(new Shapevl(shapevlCommandPath));
  }

  public FeedEvaluationStep(Shapevl shapevl) {
    this.shapevl = shapevl;
  }

  public void run(GeneratedFeedInfo info) {
    logger.info("Start feed evaluation step");
    EvaluationExtension evaluation = info.getOrCreateExtension(EvaluationExtension.class, EvaluationExtension::new);

    evaluation.setStatus(Status.PENDING);

    StopWatch stopWatch = StopWatch.createAndStart();

    Shapevl.Result shapevlResult = shapevl.run(
        info.getGeneratedFeed().getFolder(),
        info.getOriginalFeed().getFolder(),
        info.getFileStoragePath());

    stopWatch.stop();

    evaluation.setStatus(shapevlResult.hasFailed() ? Status.FAILED : Status.FINISHED);
    evaluation.setShapevlOutput(shapevlResult.getMessage());
    evaluation.setReport(info.getFileStoragePath().resolve(EvaluationExtension.SHAPEVL_REPORT));
    evaluation.setExecutionTime(Duration.of(stopWatch.getMillis(), ChronoUnit.MILLIS));

    logger.info("Finished feed evaluation step. Took {}s", stopWatch.getSeconds());
  }
}
