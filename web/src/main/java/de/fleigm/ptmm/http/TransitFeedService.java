package de.fleigm.ptmm.http;

import de.fleigm.ptmm.TransitFeed;
import io.quarkus.cache.CacheResult;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransitFeedService {

  @CacheResult(cacheName = "gtfs-feed-cache")
  public TransitFeed get(String file) {
    return new TransitFeed(file);
  }
}
