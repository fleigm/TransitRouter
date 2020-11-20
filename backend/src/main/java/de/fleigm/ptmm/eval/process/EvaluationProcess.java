package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.util.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Slf4j
@Dependent
public class EvaluationProcess {

  private final EvaluationRepository evaluationRepository;
  private final GenerateNewGtfsFeed generateNewGtfsFeed;
  private final UnzipGtfsFeed unzipGtfsFeed;
  private final EvaluateGtfsFeed evaluateGtfsFeed;
  private final GenerateQuickStats generateQuickStats;
  private final StoreEvaluationError storeEvaluationError;

  @Inject
  public EvaluationProcess(EvaluationRepository evaluationRepository,
                           GenerateNewGtfsFeed generateNewGtfsFeed,
                           UnzipGtfsFeed unzipGtfsFeed,
                           EvaluateGtfsFeed evaluateGtfsFeed,
                           GenerateQuickStats generateQuickStats,
                           StoreEvaluationError storeEvaluationError) {

    this.evaluationRepository = evaluationRepository;
    this.generateNewGtfsFeed = generateNewGtfsFeed;
    this.unzipGtfsFeed = unzipGtfsFeed;
    this.evaluateGtfsFeed = evaluateGtfsFeed;
    this.generateQuickStats = generateQuickStats;
    this.storeEvaluationError = storeEvaluationError;
  }

  public void run(Info info) {
    MDC.put("evaluation", info.getName());
    StopWatch stopWatch = StopWatch.createAndStart();
    try {
      runEvaluationProcess(info);
    } catch (Throwable error) {
      log.error("Evaluation failed.", error);
      handleEvaluationErrors(info, error);
    } finally {
      MDC.clear();
      stopWatch.stop();
      info.addStatistic("executionTime.total", stopWatch.getMillis());
      evaluationRepository.save(info);
    }
  }

  private void handleEvaluationErrors(Info info, Throwable throwable) {
    info.setStatus(Status.FAILED);

    try {
      storeEvaluationError.accept(info, throwable);
    } catch (Exception e) {
      log.error("Evaluation error handling failed.", e);
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
