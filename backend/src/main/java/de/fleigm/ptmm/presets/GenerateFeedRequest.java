package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.eval.Parameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateFeedRequest {

  @NotBlank
  private String name;

  @NotNull
  @Valid
  private Parameters parameters;
}
