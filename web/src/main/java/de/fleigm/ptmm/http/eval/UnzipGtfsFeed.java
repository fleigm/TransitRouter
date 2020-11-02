package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.util.Unzip;
import lombok.SneakyThrows;

import java.util.function.Function;

public class UnzipGtfsFeed implements Function<EvaluationProcess, EvaluationProcess> {

  @SneakyThrows
  @Override
  public EvaluationProcess apply(EvaluationProcess evaluationProcess) {
    Unzip.apply(
        evaluationProcess.getPath() + Evaluation.ORIGINAL_GTFS_FEED,
        evaluationProcess.getPath() + Evaluation.ORIGINAL_GTFS_FOLDER);

    Unzip.apply(
        evaluationProcess.getPath() + Evaluation.GENERATED_GTFS_FEED,
        evaluationProcess.getPath() + Evaluation.GENERATED_GTFS_FOLDER);

    return evaluationProcess;
  }

}
