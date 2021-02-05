package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import de.fleigm.ptmm.eval.Parameters;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.eval.process.EvaluationProcess;
import de.fleigm.ptmm.feeds.Feed;
import de.fleigm.ptmm.presets.Preset;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class EvaluationService {

  @ConfigProperty(name = "app.evaluation-tool")
  String evaluationTool;

  @ConfigProperty(name = "app.storage")
  Path storagePath;

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  TransitRouterFactory transitRouterFactory;

  public EvaluationResponse createEvaluation(CreateEvaluationRequest request) {
    GeneratedFeedInfo info = GeneratedFeedInfo.builder()
        .name(request.getName())
        .parameters(Parameters.builder()
            .sigma(request.getSigma())
            .candidateSearchRadius(request.getCandidateSearchRadius())
            .beta(request.getBeta())
            .profile(request.getProfile())
            .useGraphHopperMapMatching(request.isUseGraphHopperMapMatching())
            .build())
        .status(Status.PENDING)
        .build();

    info.setOriginalFeed(
        Feed.create(
            info.getFileStoragePath().resolve(GeneratedFeedInfo.ORIGINAL_GTFS_FEED),
            request.getGtfsFeed()));

    generatedFeedRepository.save(info);

    EvaluationProcess process = new EvaluationProcess(
        transitRouterFactory,
        generatedFeedRepository,
        evaluationTool);

    return new EvaluationResponse(info, CompletableFuture.runAsync(() -> process.run(info)));
  }

  public EvaluationResponse createFromPreset(Preset preset, String name, Parameters parameters) {
    GeneratedFeedInfo info = GeneratedFeedInfo.builder()
        .name(name)
        .parameters(parameters)
        .status(Status.PENDING)
        .preset(preset.getId())
        .originalFeed(preset.getFeed())
        .build();

    generatedFeedRepository.save(info);

    try {
      Files.createDirectories(info.getFileStoragePath());
    } catch (IOException e) {
      e.printStackTrace();
    }

    EvaluationProcess process = new EvaluationProcess(
        transitRouterFactory,
        generatedFeedRepository,
        evaluationTool);

    return new EvaluationResponse(info, CompletableFuture.runAsync(() -> process.run(info)));
  }

}
