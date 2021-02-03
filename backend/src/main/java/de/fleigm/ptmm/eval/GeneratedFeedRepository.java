package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.data.Repository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.nio.file.Path;

@Slf4j
@Singleton
public class GeneratedFeedRepository extends Repository<GeneratedFeedInfo> {


  public GeneratedFeedRepository(@ConfigProperty(name = "app.storage") Path storageLocation) {
    super(storageLocation.resolve("generated"));
  }

  @Override
  public Class<GeneratedFeedInfo> entityClass() {
    return GeneratedFeedInfo.class;
  }
}
