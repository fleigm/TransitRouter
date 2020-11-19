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
  private double sigma;
  private double beta;

  public static Parameters defaultParameters() {
    return Parameters.builder()
        .profile("bus_custom_shortest")
        .candidateSearchRadius(25)
        .sigma(25)
        .beta(2.0)
        .build();
  }
}

