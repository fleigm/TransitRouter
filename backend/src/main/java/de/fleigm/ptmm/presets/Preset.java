package de.fleigm.ptmm.presets;

import cyclops.control.Eval;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.data.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Preset implements Entity {

  @Builder.Default
  @EqualsAndHashCode.Include
  private UUID id = UUID.randomUUID();

  private String name;
  private LocalDateTime createdAt;
  private Path path;

  @ToString.Exclude
  @Setter(AccessLevel.NONE)
  private transient Eval<TransitFeed> feed = Eval.later(() -> new TransitFeed(path.resolve("gtfs")));
}
