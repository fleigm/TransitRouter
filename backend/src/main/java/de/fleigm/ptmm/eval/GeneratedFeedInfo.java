package de.fleigm.ptmm.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GeneratedFeedInfo {

  @EqualsAndHashCode.Include
  @Builder.Default
  private UUID id = UUID.randomUUID();

  private String name;
  private Parameters parameters;
  private LocalDateTime createdAt;
  private Status status;

  //@Setter(AccessLevel.NONE)
  private Path path;
  private Path originalFeed;

  @Builder.Default
  private Map<String, Object> statistics = new HashMap<>();

  @Builder.Default
  private Map<String, Object> extension = new HashMap<>();

  @Builder.Default
  private List<Error> errors = new ArrayList<>();

  public GeneratedFeedInfo addStatistic(String key, Object value) {
    statistics.put(key, value);

    return this;
  }

  public GeneratedFeedInfo addExtension(String key, Object value) {
    extension.put(key, value);

    return this;
  }

  public boolean hasExtension(String key) {
    return extension.containsKey(key);
  }

  public Object getExtension(String key) {
    return extension.get(key);
  }

  public GeneratedFeedInfo addError(Error error) {
    this.errors.add(error);
    return this;
  }

  public void setBasePath(Path path) {
    this.path = path.resolve(id.toString());
  }

  boolean hasFinished() {
    return status == Status.FINISHED;
  }

  boolean hasFailed() {
    return status == Status.FAILED;
  }

  boolean isPending() {
    return status == Status.PENDING;
  }
}
