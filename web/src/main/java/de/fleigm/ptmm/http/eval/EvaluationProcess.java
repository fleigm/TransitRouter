package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.eval.Info;
import lombok.Value;

@Value
public class EvaluationProcess {
  Info info;

  String baseFolder;

  public String getName() {
    return info.getName();
  }

  public String getPath() {
    return baseFolder + info.getName() + "/";
  }
}
