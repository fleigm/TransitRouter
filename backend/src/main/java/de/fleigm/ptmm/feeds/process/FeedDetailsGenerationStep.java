package de.fleigm.ptmm.feeds.process;

import de.fleigm.ptmm.feeds.GeneratedFeed;
import de.fleigm.ptmm.gtfs.FeedDetails;
import de.fleigm.ptmm.gtfs.TransitFeed;
import de.fleigm.ptmm.gtfs.TransitFeedService;
import de.fleigm.ptmm.util.StopWatch;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Generate {@link FeedDetails} for a {@link GeneratedFeed}.
 */
public class FeedDetailsGenerationStep implements Step {

  private final TransitFeedService transitFeedService;

  public FeedDetailsGenerationStep(TransitFeedService transitFeedService) {
    this.transitFeedService = transitFeedService;
  }

  @Override
  public void run(GeneratedFeed generatedFeed) {
    StopWatch stopWatch = StopWatch.createAndStart();

    TransitFeed transitFeed = transitFeedService.get(generatedFeed.getOriginalFeed().getPath());

    generatedFeed.addExtension(FeedDetails.extract(transitFeed));

    stopWatch.stop();

    generatedFeed
        .getOrCreateExtension(ExecutionTime.class, ExecutionTime::new)
        .add("generate_feed_details", Duration.of(stopWatch.getNanos(), ChronoUnit.NANOS));
  }
}
