package de.fleigm.ptmm.http.eval;

import lombok.SneakyThrows;

import java.io.OutputStream;
import java.util.function.Function;

public class EvaluateGtfsFeed implements Function<EvaluationProcess, EvaluationProcess> {

  @SneakyThrows
  @Override
  public EvaluationProcess apply(EvaluationProcess evaluationProcess) {
    String original = evaluationProcess.getPath() + "gtfs.original";
    String generated = evaluationProcess.getPath() + "gtfs.generated";
    String command = String.format("%sshapevl -m 3 -f %s -g %s %s", evaluationProcess.getBaseFolder(), evaluationProcess.getPath(), original, generated);

    Process process = Runtime.getRuntime().exec(command);

    OutputStream outputStream = process.getOutputStream();

    return evaluationProcess;
  }
}
