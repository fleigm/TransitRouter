package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.CompletableFuture;

@Data
@Accessors(fluent = true)
public class EvaluationResponse {
  private final GeneratedFeedInfo info;
  private final CompletableFuture<Void> process;
}
