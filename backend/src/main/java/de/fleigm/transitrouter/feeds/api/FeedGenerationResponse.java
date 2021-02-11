package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.CompletableFuture;

@Data
@Accessors(fluent = true)
public class FeedGenerationResponse {
  private final GeneratedFeed generatedFeed;
  private final CompletableFuture<Void> process;
}
