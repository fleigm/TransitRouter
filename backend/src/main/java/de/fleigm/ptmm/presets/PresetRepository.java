package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.data.Repository;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
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
                          GeneratedFeedRepository generatedFeeds) {
    super(storageLocation.resolve("presets"));
    this.generatedFeeds = generatedFeeds;
  }

  @Override
  public Class<Preset> entityClass() {
    return Preset.class;
  }

  public List<GeneratedFeedInfo> generatedFeedsFromPreset(Preset preset) {
    return generatedFeedsFromPreset(preset.getId());
  }

  public List<GeneratedFeedInfo> generatedFeedsFromPreset(UUID id) {
    return generatedFeeds.all()
        .stream()
        .filter(info -> info.getPreset().equals(id))
        .collect(Collectors.toList());
  }
}
