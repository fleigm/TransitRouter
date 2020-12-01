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

  public static EvaluationResult load(Info info) {
    if (info.getStatus() != Status.FINISHED) {
      throw new IllegalStateException("Cannot load EvaluationResult if evaluation has not finished successfully.");
    }

    return new EvaluationResult(
        Report.read(info.getPath().resolve(Evaluation.GTFS_FULL_REPORT)),
        new TransitFeed(info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED)),
        new TransitFeed(info.getPath().resolve(Evaluation.GENERATED_GTFS_FEED))
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
