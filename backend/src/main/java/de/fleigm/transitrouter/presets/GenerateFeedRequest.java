package de.fleigm.transitrouter.presets;

import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.gtfs.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateFeedRequest {

  @NotBlank
  private String name;


  // Do not use Map<Type, Parameters>, JSONB / Yasson is not able to deserialize enums as keys.
  // results in string representation at runtime.
  // See https://github.com/eclipse-ee4j/yasson/issues/283
  @NotNull
  private Map<String, Parameters> parameters;

  private boolean withEvaluation;

  public Map<Type, Parameters> getParameters() {
    return parameters.entrySet().stream()
        .collect(Collectors.toMap(entry -> Type.valueOf(entry.getKey()), Map.Entry::getValue));
  }
}
