package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Parameters;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.eval.process.EvaluationProcess;
import de.fleigm.ptmm.eval.process.ValidateGtfsFeed;
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
  String baseFolder;

  @Inject
  EvaluationRepository evaluationRepository;

  @Inject
  EvaluationProcess evaluationProcess;

  public EvaluationResponse createEvaluation(CreateEvaluationRequest request) {
    Info info = Info.builder()
        .name(request.getName())
        .createdAt(LocalDateTime.now())
        .parameters(Parameters.builder()
            .sigma(request.getSigma())
            .candidateSearchRadius(request.getCandidateSearchRadius())
            .beta(request.getBeta())
            .profile(request.getProfile())
            .build())
        .status(Status.PENDING)
        .build();

    info.setBasePath(Path.of(baseFolder));

    try {
      File file = info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED).toFile();
      FileUtils.copyInputStreamToFile(request.getGtfsFeed(), file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!ValidateGtfsFeed.validate(info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED))) {
      try {
        FileUtils.deleteDirectory(info.getPath().toFile());
      } catch (IOException e) {
        log.error("Failed to delete evaluation folder {} with invalid gtfs feed.", request.getName(), e);
      }
      throw new IllegalArgumentException("invalid gtfs feed.");
    }

    evaluationRepository.save(info);

    return new EvaluationResponse(info, CompletableFuture.runAsync(() -> evaluationProcess.run(info)));
  }

}
