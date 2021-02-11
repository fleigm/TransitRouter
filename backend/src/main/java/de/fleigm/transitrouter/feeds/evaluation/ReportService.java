package de.fleigm.transitrouter.feeds.evaluation;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.nio.file.Path;

@ApplicationScoped
public class ReportService {
  private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

  @CacheResult(cacheName = "reports")
  public Report get(Path path) {
    return Report.read(path);
  }

  @CacheInvalidateAll(cacheName = "reports")
  public void clearCache() {
    logger.info("Clear reports cache");
  }

  @CacheInvalidate(cacheName = "reports")
  public void removeFromCache(Path path) {
    logger.info("Remove report {} from reports cache.", path);
  }
}
