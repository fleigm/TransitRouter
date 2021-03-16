package de.fleigm.transitrouter.presets;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fleigm.transitrouter.data.Repository;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PresetRepository extends Repository<Preset> {

  private GeneratedFeedRepository generatedFeeds;

  protected PresetRepository() {
  }

  @Inject
  public PresetRepository(@ConfigProperty(name = "app.storage") Path storageLocation,
                          ObjectMapper objectMapper,
                          GeneratedFeedRepository generatedFeeds) {
    super(storageLocation.resolve("presets"), objectMapper);
    this.generatedFeeds = generatedFeeds;
  }

  @Override
  public Class<Preset> entityClass() {
    return Preset.class;
  }

  /**
   * Find all generated feeds of a given preset.
   *
   * @param preset preset.
   * @return generated feeds of preset.
   */
  public List<GeneratedFeed> generatedFeedsFromPreset(Preset preset) {
    return generatedFeedsFromPreset(preset.getId());
  }

  /**
   * @see PresetRepository#generatedFeedsFromPreset(Preset)
   */
  public List<GeneratedFeed> generatedFeedsFromPreset(UUID id) {
    return generatedFeeds.all()
        .stream()
        .filter(GeneratedFeed::hasPreset)
        .filter(info -> info.getPreset().equals(id))
        .collect(Collectors.toList());
  }
}
