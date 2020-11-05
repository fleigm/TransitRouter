package de.fleigm.ptmm.eval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

public class Report {

  private final List<ReportEntry> entries;
  private final double[] accuracies;

  public Report(List<ReportEntry> entries) {
    this.entries = entries;
    this.accuracies = computeAccuracies(entries);
  }

  public static Report read(Path path) {
    return read(path.toString());
  }

  public static Report read(String file) {
    try {
      List<ReportEntry> entries = Files.lines(Path.of(file))
          .map(line -> line.split("\t"))
          .map(values ->
              new ReportEntry(
                  values[0],
                  Double.parseDouble(values[1]),
                  Double.parseDouble(values[2]),
                  Double.parseDouble(values[3])))
          .collect(Collectors.toList());

      return new Report(entries);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<ReportEntry> entries() {
    return entries;
  }

  public double[] accuracies() {
    return accuracies;
  }

  public DoubleSummaryStatistics frechetDistanceStatistics() {
    return entries.stream()
        .mapToDouble(ReportEntry::avgFd)
        .summaryStatistics();
  }

  private double[] computeAccuracies(List<ReportEntry> entries) {
    double[] accuracies = new double[10];

    for (ReportEntry entry : entries) {
      for (int i = 0; i < accuracies.length; i++) {
        if (entry.an <= i * 0.1) {
          accuracies[i]++;
        }
      }
    }

    for (int i = 0; i < accuracies.length; i++) {
      accuracies[i] /= entries.size();
    }

    return accuracies;
  }
}
