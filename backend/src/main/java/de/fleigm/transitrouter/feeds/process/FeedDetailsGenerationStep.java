package de.fleigm.transitrouter.feeds.process;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.gtfs.FeedDetails;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.TransitFeedService;
import de.fleigm.transitrouter.util.StopWatch;

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
