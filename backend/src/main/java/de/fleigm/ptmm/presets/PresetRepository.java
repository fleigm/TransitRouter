package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.App;
import de.fleigm.ptmm.data.DataRoot;
import de.fleigm.ptmm.data.Repository;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PresetRepository extends Repository<Preset> {

  public PresetRepository(DataRoot dataRoot) {
    super(dataRoot);
  }

  public static class Producer {
    @Produces
    @Singleton
    public PresetRepository get(DataRoot dataRoot) {
      return dataRoot.presets();
    }
  }

  public List<GeneratedFeedInfo> generatedFeedsFromPreset(Preset preset) {
    return generatedFeedsFromPreset(preset.getId());
  }

  public List<GeneratedFeedInfo> generatedFeedsFromPreset(UUID id) {
    return App.getInstance().data().generatedFeeds().all().stream()
        .filter(info -> info.getPreset().equals(id))
        .collect(Collectors.toList());
  }
}
