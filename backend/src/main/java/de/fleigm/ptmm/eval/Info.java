package de.fleigm.ptmm.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
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
  private Status status;

  @Builder.Default
  private Map<String, Object> statistics = new HashMap<>();

  @Builder.Default
  private Map<String, Object> extension = new HashMap<>();

  public Info addStatistic(String key, Object value) {
    statistics.put(key, value);

    return this;
  }

  public Info addExtension(String key, Object value) {
    extension.put(key, value);

    return this;
  }

  public boolean hasExtension(String key) {
    return extension.containsKey(key);
  }

  public Object getExtension(String key) {
    return extension.get(key);
  }

  public Path fullPath(String evaluationFolder) {
    return Path.of(evaluationFolder, name);
  }
}
