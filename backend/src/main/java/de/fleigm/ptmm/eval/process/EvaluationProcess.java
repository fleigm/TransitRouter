package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Error;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.eval.api.TransitRouterFactory;
import de.fleigm.ptmm.util.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.file.Path;

@Slf4j
public class EvaluationProcess {

  private final GeneratedFeedRepository generatedFeedRepository;
  private final FeedGenerationStep feedGenerationStep;
  private final FeedEvaluationStep feedEvaluationStep;
  private final GenerateQuickStats generateQuickStats;

  public EvaluationProcess(TransitRouterFactory transitRouterFactory,
                           GeneratedFeedRepository generatedFeedRepository,
                           String evaluationFolder,
                           String evaluationTool) {

    this.generatedFeedRepository = generatedFeedRepository;
    this.feedGenerationStep = new FeedGenerationStep(transitRouterFactory);
    this.feedEvaluationStep = new FeedEvaluationStep(Path.of(evaluationTool));
    this.generateQuickStats = new GenerateQuickStats(evaluationFolder);
  }

  public void run(GeneratedFeedInfo info) {
    MDC.put("evaluation.id", info.getId().toString());
    MDC.put("evaluation.name", info.getName());

    log.info("Start evaluation process.");

    StopWatch stopWatch = StopWatch.createAndStart();

    try {
      runEvaluationProcess(info);
    } catch (Throwable error) {
      log.error("Evaluation failed.", error);
      info.setStatus(Status.FAILED);
      info.addError(Error.of("process.failed", "Evaluation process failed.", error));
    } finally {
      MDC.clear();
      stopWatch.stop();
      info.addStatistic("executionTime.total", stopWatch.getMillis());
      generatedFeedRepository.save(info);
      log.info("Finished evaluation process. Took {}s", stopWatch.getSeconds());
    }
  }

  private void runEvaluationProcess(GeneratedFeedInfo info) {
    feedGenerationStep.run(info);
    feedEvaluationStep.run(info);
    generateQuickStats.run(info);
    info.setStatus(Status.FINISHED);
  }
}
