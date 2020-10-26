package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.TransitFeed;

public class Evaluation {
  private Report report;
  private TransitFeed originalTransitFeed;
  private TransitFeed generatedTransitFeed;

  public Evaluation(Report report, TransitFeed originalTransitFeed, TransitFeed generatedTransitFeed) {
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
