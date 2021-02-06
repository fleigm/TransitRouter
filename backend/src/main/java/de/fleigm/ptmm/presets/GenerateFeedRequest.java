package de.fleigm.ptmm.presets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateFeedRequest {

  @NotBlank
  private String name;

  @NotBlank
  private String profile;

  @NotNull
  @Positive
  private Double sigma;

  @NotNull
  @Positive
  private Double candidateSearchRadius;

  @NotNull
  @Positive
  private Double beta;

  private boolean useGraphHopperMapMatching;

  private boolean withEvaluation;
}
