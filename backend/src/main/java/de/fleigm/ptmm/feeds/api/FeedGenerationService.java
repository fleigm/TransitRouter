package de.fleigm.ptmm.feeds.api;

import de.fleigm.ptmm.feeds.GeneratedFeed;
import de.fleigm.ptmm.feeds.GeneratedFeedRepository;
import de.fleigm.ptmm.feeds.Parameters;
import de.fleigm.ptmm.feeds.Status;
import de.fleigm.ptmm.feeds.evaluation.FeedEvaluationStep;
import de.fleigm.ptmm.feeds.evaluation.QuickStatsGenerationStep;
import de.fleigm.ptmm.feeds.process.FeedDetailsGenerationStep;
import de.fleigm.ptmm.feeds.process.FeedGenerationStep;
import de.fleigm.ptmm.feeds.process.Process;
import de.fleigm.ptmm.gtfs.Feed;
import de.fleigm.ptmm.gtfs.TransitFeedService;
import de.fleigm.ptmm.presets.Preset;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;

@ApplicationScoped
public class FeedGenerationService {
  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(FeedGenerationService.class);

  @ConfigProperty(name = "app.evaluation-tool")
  Path evaluationTool;

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  TransitRouterFactory transitRouterFactory;

  @Inject
  TransitFeedService transitFeedService;

  public FeedGenerationResponse create(GenerateFeedRequest request) {
    GeneratedFeed generatedFeed = GeneratedFeed.builder()
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

    generatedFeed.setOriginalFeed(
        Feed.create(
            generatedFeed.getFileStoragePath().resolve(GeneratedFeed.ORIGINAL_GTFS_FEED),
            request.getGtfsFeed()));

    Process process = new Process(generatedFeedRepository)
        .addStep(new FeedGenerationStep(transitRouterFactory))
        .addStep(new FeedDetailsGenerationStep(transitFeedService));

    if (request.isWithEvaluation()) {
      process.addStep(new FeedEvaluationStep(evaluationTool))
          .addStep(new QuickStatsGenerationStep());
    }

    return new FeedGenerationResponse(generatedFeed, process.runAsync(generatedFeed));
  }

  public FeedGenerationResponse createFromPreset(Preset preset,
                                                 String name,
                                                 Parameters parameters,
                                                 boolean withEvaluation) {

    GeneratedFeed generatedFeed = GeneratedFeed.builder()
        .name(name)
        .parameters(parameters)
        .status(Status.PENDING)
        .preset(preset.getId())
        .originalFeed(preset.getFeed())
        .build();

    generatedFeedRepository.save(generatedFeed);

    return run(generatedFeed, withEvaluation);
  }

  private FeedGenerationResponse run(GeneratedFeed generatedFeed, boolean withEvaluation) {
    Process process = new Process(generatedFeedRepository)
        .addStep(new FeedGenerationStep(transitRouterFactory));

    if (withEvaluation) {
      process.addStep(new FeedEvaluationStep(evaluationTool))
          .addStep(new QuickStatsGenerationStep());
    }

    return new FeedGenerationResponse(generatedFeed, process.runAsync(generatedFeed));
  }

}
