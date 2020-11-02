package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.EvaluationResult;
import de.fleigm.ptmm.eval.Report;
import io.quarkus.cache.CacheResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class EvaluationService {

  @ConfigProperty(name = "evaluation.folder")
  String baseFolder;

  @Inject
  GenerateNewGtfsFeed generateNewGtfsFeed;

  @CacheResult(cacheName = "evaluation-cache")
  public EvaluationResult get(String name) {
    TransitFeed originalTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.original.zip");
    TransitFeed generatedTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.generated.zip");
    Report report = Report.read(baseFolder + name + "/gtfs.generated.fullreport.tsv");

    return new EvaluationResult(report, originalTransitFeed, generatedTransitFeed);
  }

  public CompletableFuture<EvaluationProcess> createEvaluation(CreateEvaluationRequest request) {
    try {
      File file = new File(baseFolder + request.getName() + "/" + Evaluation.ORIGINAL_GTFS_FEED);
      FileUtils.copyInputStreamToFile(request.getGtfsFeed(), file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return CompletableFuture.supplyAsync(() -> new EvaluationProcess(request.getName(), baseFolder))
        .thenApply(generateNewGtfsFeed)
        .thenApply(new UnzipGtfsFeed())
        .thenApply(new EvaluateGtfsFeed());
            /*.handle((evaluationProcess, throwable) -> {
              log.warn("Could not finish evaluation process.", throwable);
              return evaluationProcess;
            });*/

  }
}
