package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.data.Repository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class EvaluationRepository extends Repository<GeneratedFeedInfo> {

  @ConfigProperty(name = "app.storage")
  Path storageLocation;

  public EvaluationRepository() {
  }

  public EvaluationRepository(Path storageLocation) {
    super(storageLocation);
    this.storageLocation = storageLocation;
  }

  @PostConstruct
  public void init() {
    super.init(storagePath());
  }

  @Override
  public Class<GeneratedFeedInfo> entityClass() {
    return GeneratedFeedInfo.class;
  }

  public Path storagePath() {
    return storageLocation.resolve("presets");
  }

  @CacheResult(cacheName = "evaluation-result-cache")
  public Optional<EvaluationResult> findEvaluationResult(UUID id) {
    return find(id).filter(GeneratedFeedInfo::hasFinished).map(EvaluationResult::load);
  }

  @CacheInvalidateAll(cacheName = "evaluation-result-cache")
  public void invalidateCache() {
    init();
  }
}
