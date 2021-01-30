package de.fleigm.ptmm.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.config.ConfigProvider;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Entity {

  @EqualsAndHashCode.Include
  private UUID id = UUID.randomUUID();
  private LocalDateTime createdAt = LocalDateTime.now();
  private Path fileStoragePath;

  protected Entity() {
    setStorageLocation(entityStorageRoot());
  }

  protected abstract Path entityStorageRoot();

  protected Path storageRoot() {
    return ConfigProvider.getConfig().getValue("app.storage", Path.class);
  }

  protected void setStorageLocation(Path path) {
    this.fileStoragePath = path.resolve(id.toString());
  }
}
