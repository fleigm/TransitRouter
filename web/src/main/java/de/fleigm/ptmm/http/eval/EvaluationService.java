package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Evaluation;
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
  public Evaluation get(String name) {
    TransitFeed originalTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.original.zip");
    TransitFeed generatedTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.generated.zip");
    Report report = Report.read(baseFolder + name + "/gtfs.generated.fullreport.tsv");

    return new Evaluation(report, originalTransitFeed, generatedTransitFeed);
  }

  public CompletableFuture<EvaluationProcess> createEvaluation(CreateEvaluationRequest request) {
    try {
      File file = new File(baseFolder + request.getName() + "/gtfs.original.zip");
      FileUtils.copyInputStreamToFile(request.getGtfsFeed(), file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return CompletableFuture.supplyAsync(() -> new EvaluationProcess(request.getName(), baseFolder))
            .thenApply(evaluationProcess -> {
              log.info("Generate feed");
              return evaluationProcess;
            })
            .thenApply(generateNewGtfsFeed)
            .thenApply(evaluationProcess -> {
              log.info("unzip feeds");
              return evaluationProcess;
            })
            .thenApply(new UnzipGtfsFeed())
            .thenApply(evaluationProcess -> {
              log.info("evaluate feeds");
              return evaluationProcess;
            })
            .thenApply(new EvaluateGtfsFeed())
            .thenApply(evaluationProcess -> {
              log.info("finished evaluation");
              return evaluationProcess;
            });
            /*.handle((evaluationProcess, throwable) -> {
              log.warn("Could not finish evaluation process.", throwable);
              return evaluationProcess;
            });*/

  }
}
