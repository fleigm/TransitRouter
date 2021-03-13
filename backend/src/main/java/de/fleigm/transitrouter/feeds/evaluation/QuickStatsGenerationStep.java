package de.fleigm.transitrouter.feeds.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.process.Step;
import org.slf4j.Logger;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Generate evaluation quick stats from {@link Report}
 */
public class QuickStatsGenerationStep implements Step {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(QuickStatsGenerationStep.class);

  @Override
  public void run(GeneratedFeed info) {
    logger.info("Start quick stats step.");

    Evaluation evaluation = info.getExtension(Evaluation.class).get();

    Report report = Report.read(evaluation.getReport());

    Map<String, Object> stats = new HashMap<>();
    stats.put("accuracy", computeAccuracy(report));
    stats.put("fd", buildStatsFor(report, Report.Entry::avgFd));
    stats.put("an", buildStatsFor(report, Report.Entry::an));
    stats.put("al", buildStatsFor(report, Report.Entry::al));
    evaluation.setQuickStats(stats);

    logger.info("Finished quick stats step.");
  }

  private ObjectNode buildStatsFor(Report report, ToDoubleFunction<Report.Entry> mapper) {
    DoubleSummaryStatistics stats = report.entries()
        .stream()
        .mapToDouble(mapper)
        .summaryStatistics();

    ObjectMapper objectMapper = new ObjectMapper();

    return objectMapper.createObjectNode()
        .put("min", stats.getMin())
        .put("max", stats.getMax())
        .put("average", stats.getAverage());
  }

  private double[] computeAccuracy(Report report) {
    double[] accuracies = new double[10];

    for (Report.Entry entry : report.entries()) {
      for (int i = 0; i < accuracies.length; i++) {
        if (entry.an <= i * 0.1) {
          accuracies[i]++;
        }
      }
    }

    for (int i = 0; i < accuracies.length; i++) {
      accuracies[i] /= report.entries().size();
    }

    return accuracies;
  }
}
