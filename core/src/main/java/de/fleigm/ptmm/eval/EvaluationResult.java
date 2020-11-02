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
