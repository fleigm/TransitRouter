package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.ReportEntry;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import java.util.Comparator;
import java.util.function.Consumer;

@Dependent
public class GenerateQuickStats implements Consumer<Info> {

  private final String evaluationFolder;

  public GenerateQuickStats(@ConfigProperty(name = "evaluation.folder") String evaluationFolder) {
    this.evaluationFolder = evaluationFolder;
  }

  @Override
  public void accept(Info info) {

    Report report = Report.read(info.fullPath(evaluationFolder).resolve(Evaluation.GTFS_FULL_REPORT));

    ReportEntry highestAvgFd = report.entries()
        .stream()
        .max(Comparator.comparing(ReportEntry::avgFd))
        .get();

    ReportEntry lowestAvgFd = report.entries()
        .stream()
        .min(Comparator.comparing(ReportEntry::avgFd))
        .get();

    double averageAvgFd = report.entries()
        .stream()
        .mapToDouble(ReportEntry::avgFd)
        .average()
        .getAsDouble();

    info.addStatistic("accuracy", report.accuracies())
        .addStatistic("highestAvgFd.trip", highestAvgFd.tripId)
        .addStatistic("highestAvgFd.value", highestAvgFd.avgFd)
        .addStatistic("lowestAvgFd.trip", lowestAvgFd.tripId)
        .addStatistic("lowestAvgFd.value", lowestAvgFd.avgFd)
        .addStatistic("averagedAvgFd", averageAvgFd);
  }
}
