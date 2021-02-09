package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.events.Created;
import de.fleigm.ptmm.gtfs.FeedDetails;
import de.fleigm.ptmm.gtfs.TransitFeed;
import de.fleigm.ptmm.gtfs.TransitFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

/**
 * Generate {@link FeedDetails} for the GTFS feed of a newly created {@link Preset}.
 */
@ApplicationScoped
public class GeneratePresetMetaInfo {
  private static final Logger logger = LoggerFactory.getLogger(GeneratePresetMetaInfo.class);

  @Inject
  TransitFeedService transitFeedService;

  @Inject
  PresetRepository presetRepository;

  /**
   * Generate {@link FeedDetails} for the GTFS feed of a newly created {@link Preset}.
   * This is done async.
   *
   * @param preset newly created preset
   */
  public void run(@ObservesAsync @Created Preset preset) {
    TransitFeed transitFeed = transitFeedService.get(preset.getFeed().getPath());

    preset.addExtension(FeedDetails.extract(transitFeed));

    presetRepository.save(preset);

    logger.info("Generated meta info for preset {} - {}.", preset.getName(), preset.getId());
  }
}
