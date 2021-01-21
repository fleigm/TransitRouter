package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.data.Repository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;

@ApplicationScoped
public class PresetRepository extends Repository<Preset> {

  @ConfigProperty(name = "app.storage")
  Path storageLocation;

  public PresetRepository() {
  }

  public PresetRepository(Path storageLocation) {
    super(storageLocation);
    this.storageLocation = storageLocation;
  }

  @PostConstruct
  public void init() {
    super.init(storagePath());
  }

  @Override
  public Class<Preset> entityClass() {
    return Preset.class;
  }

  public Path storagePath() {
    return storageLocation.resolve("presets");
  }
}
