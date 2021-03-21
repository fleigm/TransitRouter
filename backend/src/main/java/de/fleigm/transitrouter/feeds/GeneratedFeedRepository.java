package de.fleigm.transitrouter.feeds;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fleigm.transitrouter.data.Repository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;

@Singleton
public class GeneratedFeedRepository extends Repository<GeneratedFeed> {


  @Inject
  public GeneratedFeedRepository(@ConfigProperty(name = "app.storage") Path storageLocation,
                                 ObjectMapper objectMapper) {
    super(storageLocation.resolve("generated"), objectMapper);
  }

  @Override
  public Class<GeneratedFeed> entityClass() {
    return GeneratedFeed.class;
  }
}
