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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class EvaluationService {

  @ConfigProperty(name = "evaluation.folder")
  String baseFolder;
  @Inject
  EvaluationRepository evaluationRepository;

  @Inject
  GenerateNewGtfsFeed generateNewGtfsFeed;

  @Inject
  UnzipGtfsFeed unzipGtfsFeed;

  @Inject
  EvaluateGtfsFeed evaluateGtfsFeed;

  @Inject
  GenerateQuickStats generateQuickStats;


  @CacheResult(cacheName = "evaluation-result-cache")
  public EvaluationResult get(String name) {
    TransitFeed originalTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.original.zip");
    TransitFeed generatedTransitFeed = new TransitFeed(baseFolder + name + "/gtfs.generated.zip");
    Report report = Report.read(baseFolder + name + "/gtfs.generated.fullreport.tsv");

    return new EvaluationResult(report, originalTransitFeed, generatedTransitFeed);
  }

  public CompletableFuture<Info> createEvaluation(CreateEvaluationRequest request) {

    if (Files.exists(Path.of(baseFolder, request.getName()))) {
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
            .alpha(request.getAlpha())
            .candidateSearchRadius(request.getCandidateSearchRadius())
            .beta(request.getBeta())
            .uTurnDistancePenalty(request.getUTurnDistancePenalty())
            .profile(request.getProfile())
            .build())
        .status(Status.PENDING)
        .build();

    evaluationRepository.save(info);

    return CompletableFuture
        .runAsync(() -> generateNewGtfsFeed.accept(info))
        .thenRun(() -> unzipGtfsFeed.accept(info))
        .thenRun(() -> evaluateGtfsFeed.accept(info))
        .thenRun(() -> generateQuickStats.accept(info))
        .whenCompleteAsync((unused, throwable) -> finishEvaluationProcess(info, throwable))
        .thenApply(unused -> info);

  }

  private void finishEvaluationProcess(Info info, Throwable throwable) {
    if (throwable == null) {
      info.setStatus(Status.FINISHED);
    } else {
      log.warn("Evaluation failed!", throwable);
      info.setStatus(Status.FAILED);
    }

    evaluationRepository.save(info);
  }
}
