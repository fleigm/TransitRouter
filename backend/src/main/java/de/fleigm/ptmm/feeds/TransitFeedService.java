package de.fleigm.ptmm.feeds;


import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;

/**
 *
 */
@ApplicationScoped
public class TransitFeedService {

  private static final Logger logger = LoggerFactory.getLogger(TransitFeedService.class);

  @CacheResult(cacheName = "transit-feeds")
  public TransitFeed get(Path path) {
    return new TransitFeed(path);
  }

  @CacheInvalidateAll(cacheName = "transit-feeds")
  public void clearCache() {
    logger.info("Clear transit-feeds cache");
  }

  @CacheInvalidate(cacheName = "transit-feeds")
  public void removeFromCache(Path path) {
    logger.info("Remove transit feed {} from transit-feeds cache.", path);
  }
}
