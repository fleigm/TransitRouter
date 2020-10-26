package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Report;
import io.quarkus.cache.CacheResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EvaluationService {

  @ConfigProperty(name = "evaluation.folder")
  String baseFolder;

  @CacheResult(cacheName = "evaluation-cache")
  public Evaluation get(String name) {
    TransitFeed originalTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.original.zip");
    TransitFeed generatedTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.generated.zip");
    Report report = Report.read(baseFolder + name + "/gtfs.generated.fullreport.tsv");

    return new Evaluation(report, originalTransitFeed, generatedTransitFeed);
  }
}
