package de.fleigm.transitrouter.http;

import de.fleigm.transitrouter.feeds.evaluation.ReportService;
import de.fleigm.transitrouter.gtfs.TransitFeedService;
import de.fleigm.transitrouter.presets.CachedPatternService;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;

@Path("cache")
public class CacheController {

  @Inject
  TransitFeedService transitFeedService;

  @Inject
  ReportService reportService;

  @Inject
  CachedPatternService cachedPatternService;

  @DELETE
  public void clearCache() {
    transitFeedService.clearCache();
    reportService.clearCache();
    cachedPatternService.clearCache();
  }
}
