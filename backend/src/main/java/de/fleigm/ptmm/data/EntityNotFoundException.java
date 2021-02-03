package de.fleigm.ptmm.data;

import java.util.UUID;

/**
 * A runtime exception indicating that a entity could not be found.
 */
public class EntityNotFoundException extends RuntimeException {

  private final UUID id;
  private final Class<? extends Entity> entityType;

  /**
   * Construct a new not found exception
   *
   * @param id         entity id
   * @param entityType entity type
   */
  public EntityNotFoundException(UUID id, Class<? extends Entity> entityType) {
    this.id = id;
    this.entityType = entityType;
  }

  /**
   * Construct a new entity not found exception.
   *
   * @param id         entity id
   * @param entityType entity type
   * @return not found exception
   */
  public static EntityNotFoundException of(UUID id, Class<? extends Entity> entityType) {
    return new EntityNotFoundException(id, entityType);
  }

  public UUID id() {
    return id;
  }

  public Class<? extends Entity> entityType() {
    return entityType;
  }

  @Override
  public String toString() {
    return "EntityNotFoundException{" +
           "id=" + id +
           ", entityType=" + entityType +
           '}';
  }
}
