package de.fleigm.ptmm.data;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Repository<T extends Entity> {
  private final static Logger logger = LoggerFactory.getLogger(Repository.class);

  protected String entityFileName = "entity.json";

  private Path storageLocation;
  private Jsonb jsonb;

  private final HashMap<UUID, T> storage = new HashMap<>();

  protected Repository() {
  }

  public Repository(Path storageLocation) {
    init(storageLocation);
  }

  protected void init(Path storageLocation) {
    this.storageLocation = storageLocation;
    this.jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    ensureStorageLocationExists(storageLocation);

    storage.putAll(loadFromDisk());
  }

  private void ensureStorageLocationExists(Path storageLocation) {
    try {
      Files.createDirectories(storageLocation);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void save(T entity) {
    try {
      Path path = entityStoragePath(entity);
      Files.createDirectories(path);
      Files.writeString(path.resolve(entityFileName), toJson(entity));

      entity.setPath(path);
      storage.put(entity.getId(), entity);
    } catch (IOException e) {
      logger.error("Failed to save entity {}", entity, e);
    }
  }

  public Optional<T> find(UUID id) {
    return Optional.ofNullable(storage.get(id));
  }

  public List<T> all() {
    return new ArrayList<>(storage.values());
  }

  public void delete(UUID id) {
    find(id).ifPresent(this::removeExistingEntity);
  }

  public void delete(T entity) {
    find(entity.getId()).ifPresent(this::removeExistingEntity);
  }

  private void removeExistingEntity(T entity) {
    try {
      FileUtils.deleteDirectory(entityStoragePath(entity).toFile());
      storage.remove(entity.getId());
    } catch (IOException e) {
      logger.error("Failed to delete entity {}", entity, e);
    }
  }

  public Path entityStoragePath(T entity) {
    return storageLocation.resolve(entity.getId().toString());
  }

  public String toJson(T entity) {
    return jsonb.toJson(entity);
  }

  public T fromJson(String jsonEntity) {
    return jsonb.fromJson(jsonEntity, entityClass());
  }

  public abstract Class<T> entityClass();

  protected Map<UUID, T> loadFromDisk() {
    try {
      return Files.walk(storageLocation, 2)
          .filter(path -> path.endsWith(entityFileName))
          .map(this::loadEntityFile)
          .map(this::fromJson)
          .collect(Collectors.toMap(Entity::getId, Function.identity()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String loadEntityFile(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
