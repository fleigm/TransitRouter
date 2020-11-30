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
  EvaluationProcess evaluationProcess;

  public CompletableFuture<Info> createEvaluation(CreateEvaluationRequest request) {
    Path path = Path.of(baseFolder, request.getName());

    if (Files.exists(path)) {
      throw new IllegalArgumentException("duplicate evaluation name");
    }

    try {
      File file = new File(baseFolder + request.getName() + "/" + Evaluation.ORIGINAL_GTFS_FEED);
      FileUtils.copyInputStreamToFile(request.getGtfsFeed(), file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!ValidateGtfsFeed.validate(path.resolve(Evaluation.ORIGINAL_GTFS_FEED))) {
      try {
        FileUtils.deleteDirectory(path.toFile());
      } catch (IOException e) {
        log.error("Failed to delete evaluation folder {} with invalid gtfs feed.", request.getName(), e);
      }
      throw new IllegalArgumentException("invalid gtfs feed.");
    }


    Info info = Info.builder()
        .name(request.getName())
        .path(path)
        .createdAt(LocalDateTime.now())
        .parameters(Parameters.builder()
            .sigma(request.getSigma())
            .candidateSearchRadius(request.getCandidateSearchRadius())
            .beta(request.getBeta())
            .profile(request.getProfile())
            .build())
        .status(Status.PENDING)
        .build();

    evaluationRepository.save(info);

    return CompletableFuture
        .runAsync(() -> evaluationProcess.run(info))
        .thenApply(unused -> info);

  }
}
