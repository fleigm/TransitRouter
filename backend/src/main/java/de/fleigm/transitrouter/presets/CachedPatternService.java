package de.fleigm.transitrouter.presets;

import de.fleigm.transitrouter.Pattern;
import de.fleigm.transitrouter.gtfs.Feed;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.TransitFeedService;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Caches patterns for faster response times.
 */
@ApplicationScoped
public class CachedPatternService {

  @Inject
  TransitFeedService transitFeedService;

  @CacheResult(cacheName = "patterns")
  public List<Pattern> getPatternsForFeed(Feed feed) {
    TransitFeed transitFeed = transitFeedService.get(feed.getPath());

    return transitFeed.routes().values().stream()
        .flatMap(route -> transitFeed.findPatterns(route).stream())
        .collect(Collectors.toList());
  }

  @CacheInvalidateAll(cacheName = "patterns")
  public void clearCache(){
  }
}
