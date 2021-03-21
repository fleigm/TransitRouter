package de.fleigm.transitrouter.feeds;

import de.fleigm.transitrouter.data.Entity;
import de.fleigm.transitrouter.data.Extensions;
import de.fleigm.transitrouter.data.HasExtensions;
import de.fleigm.transitrouter.gtfs.Feed;
import de.fleigm.transitrouter.gtfs.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contains all information of a generated GTFS feed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class GeneratedFeed extends Entity implements HasExtensions {

  public static final String GENERATED_GTFS_FEED = "gtfs.generated.zip";
  public static final String GENERATED_GTFS_FOLDER = "gtfs.generated";
  public static final String ORIGINAL_GTFS_FEED = "gtfs.original.zip";
  public static final String ORIGINAL_GTFS_FOLDER = "gtfs.original";

  private String name;
  //private Parameters parameters;
  private Map<Type, Parameters> parameters;
  private Status status;

  private Feed feed;
  private Feed originalFeed;
  private UUID preset;

  @Builder.Default
  private Extensions extensions = new Extensions();

  @Builder.Default
  private List<Error> errors = new ArrayList<>();

  @Override
  public Extensions extensions() {
    return extensions;
  }

  @Override
  protected Path entityStorageRoot() {
    return storageRoot().resolve("generated");
  }

  public GeneratedFeed addError(Error error) {
    this.errors.add(error);
    return this;
  }

  public boolean hasPreset() {
    return preset != null;
  }
}
