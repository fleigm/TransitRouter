package de.fleigm.ptmm.data;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

  private final UUID uuid;
  private final Class<? extends Entity> entityClass;

  public EntityNotFoundException(UUID uuid, Class<? extends Entity> entityClass) {
    this.uuid = uuid;
    this.entityClass = entityClass;
  }

  public static EntityNotFoundException of(UUID id, Class<? extends Entity> entityClass) {
    return new EntityNotFoundException(id, entityClass);
  }
}
