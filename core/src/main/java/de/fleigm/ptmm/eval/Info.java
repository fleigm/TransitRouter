package de.fleigm.ptmm.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Info {

  @EqualsAndHashCode.Include
  private String name;
  private Parameters parameters;
  private LocalDateTime createdAt;
  private String path;
  private Status status;

  @Builder.Default
  private Map<String, Object> statistics = new HashMap<>();

  public Info addStatistic(String key, Object value) {
    statistics.put(key, value);

    return this;
  }
}
