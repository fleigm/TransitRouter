package de.fleigm.transitrouter.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fleigm.transitrouter.http.json.ObjectMapperConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

  private TestRepository repository;
  private Path testStorage;

  @BeforeEach
  void beforeEach() throws IOException {
    testStorage = Files.createTempDirectory("test-storage");
    System.setProperty("app.storage", testStorage.toString());
    repository = new TestRepository(testStorage, ObjectMapperConfiguration.get());
  }

  @Test
  void save_entity() {
    TestEntity entity = new TestEntity();
    repository.save(entity);

    assertTrue(Files.exists(entity.getFileStoragePath()));
    assertTrue(Files.exists(entity.getFileStoragePath().resolve("entity.json")));
  }

  @Test
  void saving_entity_multiple_times_does_not_create_multiple_files() throws IOException {
    TestEntity entity = new TestEntity();
    repository.save(entity);
    repository.save(entity);

    assertEquals(1, repository.all().size());
    assertEquals(1, Files.list(entity.entityStorageRoot()).count());
  }

  @Test
  void changes_persist_on_save() {
    TestEntity entity = new TestEntity();
    repository.save(entity);

    entity.setName("new name");
    repository.save(entity);

    repository.loadEntities();

    TestEntity reloadedEntity = repository.findOrFail(entity.getId());

    assertNotSame(reloadedEntity, entity);
    assertEquals("new name", reloadedEntity.getName());
  }

  @Test
  void remove_files_on_delete() {
    TestEntity entity = new TestEntity();
    repository.save(entity);

    repository.delete(entity);

    assertFalse(Files.exists(entity.getFileStoragePath()));
    assertFalse(Files.exists(entity.getFileStoragePath().resolve("entity.json")));
  }

  @Test
  void get_entity() {
    TestEntity entity = new TestEntity();
    repository.save(entity);

    assertTrue(repository.find(entity.getId()).isPresent());
    assertEquals(entity, repository.findOrFail(entity.getId()));

    assertFalse(repository.find(UUID.randomUUID()).isPresent());
    assertThrows(EntityNotFoundException.class, () -> repository.findOrFail(UUID.randomUUID()));
  }

  @Test
  void load_entities_on_creation() {
    TestEntity entity = new TestEntity();
    repository.save(entity);

    TestRepository repo = new TestRepository(testStorage, ObjectMapperConfiguration.get());

    assertFalse(repo.all().isEmpty());
    assertTrue(repo.find(entity.getId()).isPresent());
  }

  static class TestRepository extends Repository<TestEntity> {

    public TestRepository(Path storageLocation, ObjectMapper objectMapper) {
      super(storageLocation.resolve("test-entities"), objectMapper);
    }

    @Override
    public Class<TestEntity> entityClass() {
      return TestEntity.class;
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class TestEntity extends Entity {
    private String name;

    public TestEntity() {
      this("");
    }

    public TestEntity(String name) {
      this.name = name;
    }

    @Override
    protected Path entityStorageRoot() {
      return storageRoot().resolve("test-entities");
    }

    @Override
    protected Path storageRoot() {
      return super.storageRoot();
    }
  }
}