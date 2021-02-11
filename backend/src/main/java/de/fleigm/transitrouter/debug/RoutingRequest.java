package de.fleigm.transitrouter.debug;

import de.fleigm.transitrouter.routing.Observation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoutingRequest {

  @NotNull
  private List<Observation> observations;

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

}
