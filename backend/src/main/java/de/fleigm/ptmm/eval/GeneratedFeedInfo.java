package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.data.Entity;
import de.fleigm.ptmm.data.Extensions;
import de.fleigm.ptmm.data.HasExtensions;
import de.fleigm.ptmm.feeds.Feed;
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
public class GeneratedFeedInfo implements Entity, HasExtensions {

  public static final String GENERATED_GTFS_FEED = "gtfs.generated.zip";
  public static final String GENERATED_GTFS_FOLDER = "gtfs.generated";

  @Builder.Default
  @EqualsAndHashCode.Include
  private UUID id = UUID.randomUUID();

  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  private String name;
  private Parameters parameters;
  private Status status;

  private Path fileStoragePath;
  private Feed generatedFeed;
  private Feed originalFeed;
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

  public boolean hasFinished() {
    return status == Status.FINISHED;
  }

  public boolean hasFailed() {
    return status == Status.FAILED;
  }

  public boolean isPending() {
    return status == Status.PENDING;
  }

  public Path getFileStoragePath() {
    return fileStoragePath.resolve(id.toString());
  }
}
