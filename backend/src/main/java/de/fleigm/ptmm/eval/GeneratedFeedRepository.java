package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.data.Repository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

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

  @CacheResult(cacheName = "evaluation-result-cache")
  public Optional<EvaluationResult> findEvaluationResult(UUID id) {
    return find(id).filter(GeneratedFeedInfo::hasFinished).map(EvaluationResult::load);
  }

  @CacheInvalidateAll(cacheName = "evaluation-result-cache")
  public void invalidateCache() {
  }
}
