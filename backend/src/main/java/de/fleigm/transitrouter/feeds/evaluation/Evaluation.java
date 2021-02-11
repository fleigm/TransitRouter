package de.fleigm.transitrouter.feeds.evaluation;

import de.fleigm.transitrouter.data.Extension;
import de.fleigm.transitrouter.feeds.Status;
import lombok.Data;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;


/**
 * Contains evaluation data of a generated GTFS feed.
 */
@Data
public class Evaluation implements Extension {

  public static final String SHAPEVL_REPORT = "gtfs.generated.fullreport.tsv";

  private Status status;
  private Path report;
  private String shapevlOutput;
  private Map<String, Object> quickStats;
  private Duration executionTime;
}
