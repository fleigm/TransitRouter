package de.fleigm.transitrouter.feeds.evaluation;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.feeds.process.ExecutionTime;
import de.fleigm.transitrouter.feeds.process.Step;
import de.fleigm.transitrouter.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Run feed evaluation
 */
public class FeedEvaluationStep implements Step {
  private static final Logger logger = LoggerFactory.getLogger(FeedEvaluationStep.class);
  private final Shapevl shapevl;

  public FeedEvaluationStep(Path shapevlCommandPath) {
    this(new Shapevl(shapevlCommandPath));
  }

  public FeedEvaluationStep(Shapevl shapevl) {
    this.shapevl = shapevl;
  }

  @Override
  public void run(GeneratedFeed info) {
    logger.info("Start feed evaluation step");
    Evaluation evaluation = info.getOrCreateExtension(Evaluation.class, Evaluation::new);

    evaluation.setStatus(Status.PENDING);

    StopWatch stopWatch = StopWatch.createAndStart();

    Shapevl.Result shapevlResult = shapevl.run(
        info.getFeed().getFolder(),
        info.getOriginalFeed().getFolder(),
        info.getFileStoragePath());

    stopWatch.stop();

    evaluation.setStatus(shapevlResult.hasFailed() ? Status.FAILED : Status.FINISHED);
    evaluation.setShapevlOutput(shapevlResult.getMessage());
    evaluation.setReport(info.getFileStoragePath().resolve(Evaluation.SHAPEVL_REPORT));
    evaluation.setExecutionTime(Duration.of(stopWatch.getMillis(), ChronoUnit.MILLIS));

    info.getOrCreateExtension(ExecutionTime.class, ExecutionTime::new)
        .add("feed_evaluation", Duration.of(stopWatch.getNanos(), ChronoUnit.NANOS));

    logger.info("Finished feed evaluation step. Took {}s", stopWatch.getSeconds());
  }
}
