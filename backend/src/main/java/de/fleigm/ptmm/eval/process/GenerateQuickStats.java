package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.ReportEntry;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.json.Json;
import javax.json.JsonObject;
import java.util.DoubleSummaryStatistics;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

@Dependent
public class GenerateQuickStats implements Consumer<Info> {

  private final String evaluationFolder;

  public GenerateQuickStats(@ConfigProperty(name = "evaluation.folder") String evaluationFolder) {
    this.evaluationFolder = evaluationFolder;
  }

  @Override
  public void accept(Info info) {
    Report report = Report.read(info.getPath().resolve(Evaluation.GTFS_FULL_REPORT));

    info.addStatistic("accuracy", computeAccuracy(report))
        .addStatistic("fd", buildStatsFor(report, ReportEntry::avgFd))
        .addStatistic("an", buildStatsFor(report, ReportEntry::an))
        .addStatistic("al", buildStatsFor(report, ReportEntry::al));
  }

  private JsonObject buildStatsFor(Report report, ToDoubleFunction<ReportEntry> mapper) {
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

    for (ReportEntry entry : report.entries()) {
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
