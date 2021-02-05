package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.data.Entity;
import de.fleigm.ptmm.data.Extensions;
import de.fleigm.ptmm.data.HasExtensions;
import de.fleigm.ptmm.gtfs.Feed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * Preset of a GTFS feed that allow the generation and comparison of new GTFS feeds.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Preset extends Entity implements HasExtensions {
  private String name;
  private Feed feed;

  @Builder.Default
  private Extensions extensions = new Extensions();

  @Override
  public Extensions extensions() {
    return extensions;
  }

  @Override
  protected Path entityStorageRoot() {
    return storageRoot().resolve("presets");
  }
}
