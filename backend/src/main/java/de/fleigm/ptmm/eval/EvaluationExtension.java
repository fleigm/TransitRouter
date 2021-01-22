package de.fleigm.ptmm.eval;

import lombok.Data;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;


@Data
public class EvaluationExtension {

  public static final String SHAPEVL_REPORT = "gtfs.generated.fullreport.tsv";

  private Status status;
  private Path report;
  private String shapevlOutput;
  private Map<String, Object> quickStats;
  private Duration executionTime;
}
