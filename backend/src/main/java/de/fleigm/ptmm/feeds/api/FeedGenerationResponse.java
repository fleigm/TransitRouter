package de.fleigm.ptmm.feeds.api;

import de.fleigm.ptmm.feeds.GeneratedFeed;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.CompletableFuture;

@Data
@Accessors(fluent = true)
public class FeedGenerationResponse {
  private final GeneratedFeed generatedFeed;
  private final CompletableFuture<Void> process;
}
