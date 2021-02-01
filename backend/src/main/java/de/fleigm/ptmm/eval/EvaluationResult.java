package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.TransitFeed;

public class EvaluationResult {
  private final Report report;
  private final TransitFeed originalTransitFeed;
  private final TransitFeed generatedTransitFeed;

  public EvaluationResult(Report report, TransitFeed originalTransitFeed, TransitFeed generatedTransitFeed) {
    this.report = report;
    this.originalTransitFeed = originalTransitFeed;
    this.generatedTransitFeed = generatedTransitFeed;
  }

  public static EvaluationResult load(GeneratedFeedInfo info) {
    if (info.getStatus() != Status.FINISHED || !info.hasExtension(EvaluationExtension.class)) {
      throw new IllegalStateException("Cannot load EvaluationResult if evaluation has not finished successfully.");
    }

    EvaluationExtension evaluation = info.getExtension(EvaluationExtension.class).get();

    return new EvaluationResult(
        Report.read(evaluation.getReport()),
        new TransitFeed(info.getOriginalFeed().getPath()),
        new TransitFeed(info.getGeneratedFeed().getPath())
    );
  }

  public Report report() {
    return report;
  }

  public TransitFeed originalTransitFeed() {
    return originalTransitFeed;
  }

  public TransitFeed generatedTransitFeed() {
    return generatedTransitFeed;
  }
}
