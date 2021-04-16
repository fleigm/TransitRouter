package de.fleigm.transitrouter.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.config.ConfigProvider;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Provide basic functionality for all entities
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Entity {

  @EqualsAndHashCode.Include
  private UUID id = UUID.randomUUID();
  private LocalDateTime createdAt = LocalDateTime.now();
  private Path fileStoragePath; // rename to entityStoragePath?

  protected Entity() {
    setStorageLocation(entityStorageRoot());
  }

  /**
   * Directory where all entities of that type are stored.
   * Combine this with {@link Entity#storageRoot()}.
   *
   * @return entity storage root
   */
  protected abstract Path entityStorageRoot();

  /**
   * @return storage root of the application
   */
  protected Path storageRoot() {
    return ConfigProvider.getConfig().getValue("app.storage", Path.class);
  }

  /**
   * Set the storage location of an entity.
   * This automatically append the {@link Entity#id} to the path.
   *
   * @param entityStorageRoot entity storage root
   */
  protected void setStorageLocation(Path entityStorageRoot) {
    this.fileStoragePath = entityStorageRoot.resolve(id.toString());
  }
}
