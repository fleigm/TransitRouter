package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.ReportEntry;

import java.util.Comparator;
import java.util.function.Function;

public class GenerateQuickStats implements Function<EvaluationProcess, EvaluationProcess> {

  @Override
  public EvaluationProcess apply(EvaluationProcess evaluationProcess) {

    Report report = Report.read(evaluationProcess.getPath() + Evaluation.GTFS_FULL_REPORT);

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

    evaluationProcess.getInfo()
        .addStatistic("accuracy", report.accuracies())
        .addStatistic("highestAvgFd.trip", highestAvgFd.tripId)
        .addStatistic("highestAvgFd.value", highestAvgFd.avgFd)
        .addStatistic("lowestAvgFd.trip", lowestAvgFd.tripId)
        .addStatistic("lowestAvgFd.value", lowestAvgFd.avgFd)
        .addStatistic("averagedAvgFd", averageAvgFd);

    return evaluationProcess;
  }
}
