package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Info;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.CompletableFuture;

@Data
@Accessors(fluent = true)
public class EvaluationResponse {
  private final Info info;
  private final CompletableFuture<Void> process;
}
