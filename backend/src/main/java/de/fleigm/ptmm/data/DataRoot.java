package de.fleigm.ptmm.data;

import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import de.fleigm.ptmm.presets.PresetRepository;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class DataRoot {
  private final GeneratedFeedRepository generatedFeeds = new GeneratedFeedRepository();
  private final PresetRepository presets = new PresetRepository();

  @Produces
  public GeneratedFeedRepository generatedFeeds() {
    return generatedFeeds;
  }

  @Produces
  public PresetRepository presets() {
    return presets;
  }
}
