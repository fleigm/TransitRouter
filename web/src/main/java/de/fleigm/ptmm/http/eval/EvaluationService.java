package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.EvaluationResult;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Parameters;
import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.Status;
import io.quarkus.cache.CacheResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class EvaluationService {

  @ConfigProperty(name = "evaluation.folder")
  String baseFolder;

  @Inject
  GenerateNewGtfsFeed generateNewGtfsFeed;

  @Inject
  EvaluationRepository evaluationRepository;

  @CacheResult(cacheName = "evaluation-result-cache")
  public EvaluationResult get(String name) {
    TransitFeed originalTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.original.zip");
    TransitFeed generatedTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.generated.zip");
    Report report = Report.read(baseFolder + name + "/gtfs.generated.fullreport.tsv");

    return new EvaluationResult(report, originalTransitFeed, generatedTransitFeed);
  }

  public CompletableFuture<EvaluationProcess> createEvaluation(CreateEvaluationRequest request) {
    if (evaluationRepository.find(request.getName()).isPresent()) {
      throw new IllegalArgumentException("duplicate evaluation name");
    }

    try {
      File file = new File(baseFolder + request.getName() + "/" + Evaluation.ORIGINAL_GTFS_FEED);
      FileUtils.copyInputStreamToFile(request.getGtfsFeed(), file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Info info = Info.builder()
        .name(request.getName())
        .createdAt(LocalDateTime.now())
        .parameters(Parameters.builder()
            .alpha(25)
            .candidateSearchRadius(25)
            .beta(2.0)
            .uTurnDistancePenalty(1500)
            .profile("bus_custom_shortest")
            .build())
        .status(Status.PENDING)
        .build();

    evaluationRepository.save(info);

    return CompletableFuture.supplyAsync(() -> new EvaluationProcess(info, baseFolder))
        .thenApply(generateNewGtfsFeed)
        .thenApply(new UnzipGtfsFeed())
        .thenApply(new EvaluateGtfsFeed())
        .thenApply(new GenerateQuickStats())
        .thenApply(evaluationProcess -> {
          evaluationRepository.save(evaluationProcess.getInfo());
          return evaluationProcess;
        });
            /*.handle((evaluationProcess, throwable) -> {
              log.warn("Could not finish evaluation process.", throwable);
              return evaluationProcess;
            });*/

  }
}
