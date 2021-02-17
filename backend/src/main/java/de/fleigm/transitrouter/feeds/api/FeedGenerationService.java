package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.feeds.evaluation.FeedEvaluationStep;
import de.fleigm.transitrouter.feeds.evaluation.QuickStatsGenerationStep;
import de.fleigm.transitrouter.feeds.process.FeedDetailsGenerationStep;
import de.fleigm.transitrouter.feeds.process.FeedGenerationStep;
import de.fleigm.transitrouter.feeds.process.Process;
import de.fleigm.transitrouter.gtfs.Feed;
import de.fleigm.transitrouter.gtfs.TransitFeedService;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.presets.Preset;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Map;

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
        .parameters(Map.of(Type.BUS, Parameters.builder()
            .sigma(request.getSigma())
            .candidateSearchRadius(request.getCandidateSearchRadius())
            .beta(request.getBeta())
            .profile(request.getProfile())
            .useGraphHopperMapMatching(request.isUseGraphHopperMapMatching())
            .build()))
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
                                                 Map<Type, Parameters> parameters,
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
