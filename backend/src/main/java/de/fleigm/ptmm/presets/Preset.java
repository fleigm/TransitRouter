package de.fleigm.ptmm.presets;

import cyclops.control.Eval;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.data.Entity;
import de.fleigm.ptmm.data.Extensions;
import de.fleigm.ptmm.data.HasExtensions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Preset extends Entity implements HasExtensions {

  private String name;
  private LocalDateTime createdAt;

  @Builder.Default
  private Extensions extensions = new Extensions();

  @ToString.Exclude
  @Setter(AccessLevel.NONE)
  private transient Eval<TransitFeed> feed = Eval.later(() -> new TransitFeed(getPath().resolve("gtfs")));

  @Override
  public Extensions extensions() {
    return extensions;
  }
}
