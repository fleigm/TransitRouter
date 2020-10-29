package de.fleigm.ptmm.http.eval;

import lombok.Value;

@Value
public class EvaluationProcess {
  String name;

  String baseFolder;

  public String getPath() {
    return baseFolder + name + "/";
  }
}
