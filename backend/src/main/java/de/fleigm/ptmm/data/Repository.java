package de.fleigm.ptmm.data;

import de.fleigm.ptmm.http.json.ExtensionsDeserializer;
import de.fleigm.ptmm.http.json.ExtensionsSerializer;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

  public Repository(@ConfigProperty(name = "app.storage") Path storageLocation) {
    init(storageLocation);
  }

  protected void init(Path storageLocation) {
    logger.info("init repo for {}", entityClass());
    this.storageLocation = storageLocation;
    this.jsonb = JsonbBuilder.create(new JsonbConfig()
        .withSerializers(new ExtensionsSerializer())
        .withDeserializers(new ExtensionsDeserializer())
        .withFormatting(true));

    ensureStorageLocationExists(storageLocation);

    storage.clear();
    Map<UUID, T> reloadedEntities = loadFromDisk();
    storage.putAll(reloadedEntities);
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
      //entity.setStorageLocation(storageLocation);
      Files.createDirectories(entity.getFileStoragePath());
      Files.writeString(entity.getFileStoragePath().resolve(entityFileName), toJson(entity));

      storage.put(entity.getId(), entity);
    } catch (IOException e) {
      logger.error("Failed to save entity {}", entity, e);
    }
  }

  public Optional<T> find(UUID id) {
    return Optional.ofNullable(storage.get(id));
  }

  public T findOrFail(UUID id) {
    T entity = storage.get(id);

    if (entity == null) {
      throw EntityNotFoundException.of(id, entityClass());
    }

    return entity;
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
      FileUtils.deleteDirectory(entity.getFileStoragePath().toFile());
      storage.remove(entity.getId());
    } catch (IOException e) {
      logger.error("Failed to delete entity {}", entity, e);
    }
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
