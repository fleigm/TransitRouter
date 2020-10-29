package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.util.Unzip;
import lombok.SneakyThrows;

import java.util.function.Function;

public class UnzipGtfsFeed implements Function<EvaluationProcess, EvaluationProcess> {

  @SneakyThrows
  @Override
  public EvaluationProcess apply(EvaluationProcess evaluationProcess) {
    Unzip.apply(
        evaluationProcess.getPath() + "gtfs.original.zip",
        evaluationProcess.getPath() + "gtfs.original");

    Unzip.apply(
        evaluationProcess.getPath() + "gtfs.generated.zip",
        evaluationProcess.getPath() + "gtfs.generated");

    return evaluationProcess;
  }

}
