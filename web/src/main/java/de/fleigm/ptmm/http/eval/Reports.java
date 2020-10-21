package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class Reports {
  private static final Logger LOGGER = LoggerFactory.getLogger(Reports.class);

  private final Map<String, Report> reports = new HashMap<>();


  public Report get(String name) {
    return reports.computeIfAbsent(name, this::loadReport);
  }

  private Report loadReport(String name) {
    LOGGER.info("Loading Report {}.", name);
    StopWatch stopWatch = StopWatch.createAndStart();

    Report report = Report.read("../../../" + name + "/gtfs.generated.fullreport.tsv");

    stopWatch.stop();
    LOGGER.info("Finished loading Report {}. Took {}ms", name, stopWatch.getMillis());

    return report;
  }

}
