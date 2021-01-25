package de.fleigm.ptmm.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryTest {

  private Path path;

  @BeforeEach
  void beforeEach() throws IOException {
    this.path = Files.createTempDirectory("repositoryTest");
  }

  @Test
  void load_entities_on_creation() {
    RepositoryStub repo = new RepositoryStub(path);
    EntityStub entity = new EntityStub();
    repo.save(entity);

    RepositoryStub newRepo = new RepositoryStub(path);
    assertEquals(1, newRepo.all().size());
  }

  @Test
  void persist_changes_on_save() {
    RepositoryStub repo = new RepositoryStub(path);
    EntityStub entity = new EntityStub("test");
    repo.save(entity);

    entity.setName("new name");
    repo.save(entity);

    EntityStub reloadedEntity = repo.fromJson(repo.loadEntityFile(entity.getPath().resolve(repo.entityFileName)));

    assertEquals("new name", reloadedEntity.getName());
  }


  private static class RepositoryStub extends Repository<EntityStub> {

    public RepositoryStub(Path storageLocation) {
      super(storageLocation);
    }

    @Override
    public Class<EntityStub> entityClass() {
      return EntityStub.class;
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class EntityStub extends Entity {
    private String name;

    public EntityStub() {
    }

    public EntityStub(String name) {
      this.name = name;
    }
  }
}