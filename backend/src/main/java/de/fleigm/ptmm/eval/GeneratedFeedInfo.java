package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.data.Entity;
import de.fleigm.ptmm.data.Extensions;
import de.fleigm.ptmm.data.HasExtensions;
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
@EqualsAndHashCode(callSuper=true)
public class GeneratedFeedInfo extends Entity implements HasExtensions {

  public static final String GENERATED_GTFS_FEED = "gtfs.generated.zip";
  public static final String GENERATED_GTFS_FOLDER = "gtfs.generated";

  private String name;
  private Parameters parameters;
  private LocalDateTime createdAt;
  private Status status;

  private Path generatedFeed;
  private Path originalFeed;
  private UUID preset;

  @Builder.Default
  private Map<String, Object> statistics = new HashMap<>();

  @Builder.Default
  private Extensions extensions = new Extensions();

  @Builder.Default
  private List<Error> errors = new ArrayList<>();

  @Override
  public Extensions extensions() {
    return extensions;
  }

  public GeneratedFeedInfo addStatistic(String key, Object value) {
    statistics.put(key, value);

    return this;
  }

  public GeneratedFeedInfo addError(Error error) {
    this.errors.add(error);
    return this;
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
