package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.data.DataRoot;
import de.fleigm.ptmm.data.Repository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class GeneratedFeedRepository extends Repository<GeneratedFeedInfo> {

  @CacheResult(cacheName = "evaluation-result-cache")
  public Optional<EvaluationResult> findEvaluationResult(UUID id) {
    return find(id).filter(GeneratedFeedInfo::hasFinished).map(EvaluationResult::load);
  }

  @CacheInvalidateAll(cacheName = "evaluation-result-cache")
  public void invalidateCache() {
  }

  public static class Producer {
    @Produces
    @ApplicationScoped
    public GeneratedFeedRepository get(DataRoot dataRoot) {
      return dataRoot.generatedFeeds();
    }
  }
}
