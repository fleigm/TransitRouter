package de.fleigm.ptmm.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parameters {
  private String profile;
  private double candidateSearchRadius;
  private double alpha;
  private double beta;
  private double uTurnDistancePenalty;
}

