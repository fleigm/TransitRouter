package de.fleigm.ptmm.feeds.process;

import de.fleigm.ptmm.feeds.GeneratedFeed;
import de.fleigm.ptmm.gtfs.FeedDetails;
import de.fleigm.ptmm.gtfs.TransitFeed;
import de.fleigm.ptmm.gtfs.TransitFeedService;

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
    TransitFeed transitFeed = transitFeedService.get(generatedFeed.getOriginalFeed().getPath());

    generatedFeed.addExtension(FeedDetails.extract(transitFeed));
  }
}
