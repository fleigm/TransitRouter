package de.fleigm.ptmm.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Entity {

  @EqualsAndHashCode.Include
  private UUID id = UUID.randomUUID();

  private Path path;

  public void setStoragePath(Path path) {
    this.path = path.resolve(id.toString());
  }
}
