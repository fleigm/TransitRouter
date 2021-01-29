package de.fleigm.ptmm.presets;

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
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Preset implements Entity, HasExtensions {

  @Builder.Default
  @EqualsAndHashCode.Include
  private UUID id = UUID.randomUUID();

  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  private String name;
  private Feed feed;

  private Path fileStoragePath;

  @Builder.Default
  private Extensions extensions = new Extensions();

  @Override
  public Extensions extensions() {
    return extensions;
  }

  public Path getFileStoragePath() {
    return fileStoragePath.resolve(id.toString());
  }
}
