package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Error;
import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.routing.TransitRouterFactory;
import de.fleigm.ptmm.util.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class EvaluationProcess {

  private final EvaluationRepository evaluationRepository;
  private final GenerateNewGtfsFeed generateNewGtfsFeed;
  private final UnzipGtfsFeed unzipGtfsFeed;
  private final EvaluateGtfsFeed evaluateGtfsFeed;
  private final GenerateQuickStats generateQuickStats;

  public EvaluationProcess(TransitRouterFactory transitRouterFactory,
                           EvaluationRepository evaluationRepository,
                           String evaluationFolder,
                           String evaluationTool) {

    this.evaluationRepository = evaluationRepository;
    this.generateNewGtfsFeed = new GenerateNewGtfsFeed(transitRouterFactory);
    this.unzipGtfsFeed = new UnzipGtfsFeed(evaluationFolder);
    this.evaluateGtfsFeed = new EvaluateGtfsFeed(evaluationTool);
    this.generateQuickStats = new GenerateQuickStats(evaluationFolder);
  }

  public void run(Info info) {
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
      evaluationRepository.save(info);
      log.info("Finished evaluation process. Took {}s", stopWatch.getSeconds());
    }
  }

  private void runEvaluationProcess(Info info) {
    generateNewGtfsFeed.accept(info);
    unzipGtfsFeed.accept(info);
    evaluateGtfsFeed.accept(info);
    generateQuickStats.accept(info);
    info.setStatus(Status.FINISHED);
  }
}
