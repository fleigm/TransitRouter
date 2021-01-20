package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.DistanceUnit;
import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.Parameters;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.eval.process.EvaluationProcess;
import de.fleigm.ptmm.eval.process.ValidateGtfsFeed;
import de.fleigm.ptmm.util.Unzip;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class EvaluationService {

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  @ConfigProperty(name = "evaluation.tool")
  String evaluationTool;

  @Inject
  EvaluationRepository evaluationRepository;

  @Inject
  TransitRouterFactory transitRouterFactory;

  public EvaluationResponse createEvaluation(CreateEvaluationRequest request) {
    GeneratedFeedInfo info = GeneratedFeedInfo.builder()
        .name(request.getName())
        .createdAt(LocalDateTime.now())
        .parameters(Parameters.builder()
            .sigma(request.getSigma())
            .candidateSearchRadius(request.getCandidateSearchRadius())
            .beta(request.getBeta())
            .profile(request.getProfile())
            .distanceUnit(request.getDistanceUnit() != null ? request.getDistanceUnit() : DistanceUnit.METERS)
            .build())
        .status(Status.PENDING)
        .build();

    info.setBasePath(Path.of(evaluationFolder));
    info.setOriginalFeed(info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED));

    try {
      File file = info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED).toFile();
      FileUtils.copyInputStreamToFile(request.getGtfsFeed(), file);

      if (!ValidateGtfsFeed.validate(info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED))) {
        FileUtils.deleteDirectory(info.getPath().toFile());
        throw new IllegalArgumentException("invalid gtfs feed.");
      }

      Unzip.apply(
          info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED),
          info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FOLDER));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    evaluationRepository.save(info);

    EvaluationProcess process = new EvaluationProcess(
        transitRouterFactory,
        evaluationRepository,
        evaluationFolder,
        evaluationTool);

    return new EvaluationResponse(info, CompletableFuture.runAsync(() -> process.run(info)));
  }

}
