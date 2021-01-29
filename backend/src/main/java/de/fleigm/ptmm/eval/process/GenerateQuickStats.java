package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.Report;
import lombok.extern.slf4j.Slf4j;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

@Slf4j
public class GenerateQuickStats {

  private final String evaluationFolder;

  public GenerateQuickStats(String evaluationFolder) {
    this.evaluationFolder = evaluationFolder;
  }

  public void run(GeneratedFeedInfo info) {
    log.info("Start quick stats step.");

    EvaluationExtension evaluation = info.getExtension(EvaluationExtension.class).get();

    Report report = Report.read(evaluation.getReport());

    Map<String, Object> stats = new HashMap<>();
    stats.put("accuracy", computeAccuracy(report));
    stats.put("fd", buildStatsFor(report, Report.Entry::avgFd).toString());
    stats.put("an", buildStatsFor(report, Report.Entry::an).toString());
    stats.put("al", buildStatsFor(report, Report.Entry::al).toString());
    evaluation.setQuickStats(stats);

    log.info("Finished quick stats step.");
  }

  private JsonObject buildStatsFor(Report report, ToDoubleFunction<Report.Entry> mapper) {
    DoubleSummaryStatistics stats = report.entries()
        .stream()
        .mapToDouble(mapper)
        .summaryStatistics();

    return Json.createObjectBuilder()
        .add("min", stats.getMin())
        .add("max", stats.getMax())
        .add("average", stats.getAverage())
        .build();
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
