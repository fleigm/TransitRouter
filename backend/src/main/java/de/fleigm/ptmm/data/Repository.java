package de.fleigm.ptmm.data;

import de.fleigm.ptmm.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class Repository<T extends Entity> {
  private final static Logger logger = LoggerFactory.getLogger(Repository.class);

  private final HashMap<UUID, T> storage = new HashMap<>();

  private void persist() {
    App.getInstance().storageManager().store(this.storage);
  }

  public void save(T entity) {
    storage.put(entity.getId(), entity);
    persist();
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
    storage.remove(entity.getId());
    persist();
  }
}
