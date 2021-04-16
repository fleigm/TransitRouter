package de.fleigm.transitrouter.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base repository for data access to entities.
 * Entities are serialized to json and stored on the file system.
 *
 * @param <T> entity type.
 */
public abstract class Repository<T extends Entity> {
  private final static Logger logger = LoggerFactory.getLogger(Repository.class);

  protected String entityFileName = "entity.json";

  private Path storageLocation;
  private ObjectMapper objectMapper;

  private final HashMap<UUID, T> storage = new HashMap<>();

  protected Repository() {
  }

  /**
   * Create a new Repository and initialize it.
   *
   * @param storageLocation storage location.
   */
  public Repository(@ConfigProperty(name = "app.storage") Path storageLocation,
                    ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    init(storageLocation);
  }

  /**
   * Initialize entity repository.
   * Ensures that the storage location exists and that all existing entities are loaded.
   *
   * @param storageLocation storage location.
   */
  protected void init(Path storageLocation) {
    logger.info("init repo for {}", entityClass());
    this.storageLocation = storageLocation;

    ensureStorageLocationExists(storageLocation);

    loadEntities();
  }

  protected void loadEntities() {
    Map<UUID, T> reloadedEntities = loadFromDisk();
    storage.clear();
    storage.putAll(reloadedEntities);
  }

  private void ensureStorageLocationExists(Path storageLocation) {
    try {
      Files.createDirectories(storageLocation);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Persist entity to file and add it to the repository.
   *
   * @param entity entity to be saved.
   */
  public void save(T entity) {
    try {
      Files.createDirectories(entity.getFileStoragePath());
      Files.writeString(entity.getFileStoragePath().resolve(entityFileName), toJson(entity));

      storage.put(entity.getId(), entity);
    } catch (IOException e) {
      logger.error("Failed to save entity {}", entity, e);
    }
  }

  /**
   * Find an entity by its id.
   *
   * @param id entity id
   * @return optional entity.
   */
  public Optional<T> find(UUID id) {
    return Optional.ofNullable(storage.get(id));
  }

  /**
   * Return an existing entity by its id or throw an {@link EntityNotFoundException}
   * if no entity with the given id exists.
   *
   * @param id entity id
   * @return existing entity
   */
  public T findOrFail(UUID id) {
    T entity = storage.get(id);

    if (entity == null) {
      throw EntityNotFoundException.of(id, entityClass());
    }

    return entity;
  }

  /**
   * Return a new list of all entities in the repository.
   *
   * @return entities
   */
  public List<T> all() {
    return new ArrayList<>(storage.values());
  }

  /**
   * Remove an entity from the repository and delete all its files.
   * If the entity does not exists in the repository nothing happens.
   *
   * @param id entity to be deleted.
   */
  public void delete(UUID id) {
    find(id).ifPresent(this::removeExistingEntity);
  }

  /**
   * Remove an entity from the repository and delete all its files.
   * If the entity does not exists in the repository nothing happens.
   *
   * @param entity entity to be deleted.
   */
  public void delete(T entity) {
    delete(entity.getId());
  }

  private void removeExistingEntity(T entity) {
    try {
      FileUtils.deleteDirectory(entity.getFileStoragePath().toFile());
      storage.remove(entity.getId());
    } catch (IOException e) {
      logger.error("Failed to delete entity {}", entity, e);
    }
  }

  /**
   * Serialize an given entity to json.
   *
   * @param entity entity to be serialized.
   * @return json representation of entity.
   */
  public String toJson(T entity) {
    try {
      return objectMapper.writeValueAsString(entity);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Create an entity of type T from json.
   *
   * @param jsonEntity json representation of entity.
   * @return deserialized entity
   */
  public T fromJson(String jsonEntity) {
    try {
      return objectMapper.readValue(jsonEntity, entityClass());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return entity type of the repository
   */
  public abstract Class<T> entityClass();

  /**
   * Load and deserialize entities from file storage.
   *
   * @return deserialized entities.
   */
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

  /**
   * Load json representation of a single entity from the file storage.
   *
   * @param path to entity file
   * @return json representation of entity
   */
  protected String loadEntityFile(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
