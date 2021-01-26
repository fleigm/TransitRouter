package de.fleigm.ptmm.data;

import de.fleigm.ptmm.App;
import de.fleigm.ptmm.presets.Preset;
import de.fleigm.ptmm.presets.PresetRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepositoryTest {
  private static App app = new App();

  private Path path;

  @BeforeEach
  void beforeEach() throws IOException {
    this.path = Files.createTempDirectory("repositoryTest");
  }

  @Test
  void load_entities_on_creation() {
    RepositoryStub repo = new RepositoryStub();
    EntityStub entity = new EntityStub();
    repo.save(entity);

    RepositoryStub newRepo = new RepositoryStub();
    assertEquals(1, newRepo.all().size());
  }

  @Test
  void persist_changes_on_save() {
    RepositoryStub repo = new RepositoryStub();
    EntityStub entity = new EntityStub("test");
    repo.save(entity);

    entity.setName("new name");
    repo.save(entity);

/*    EntityStub reloadedEntity = repo.fromJson(repo.loadEntityFile(entity.getPath().resolve(repo.entityFileName)));

    assertEquals("new name", reloadedEntity.getName());*/
  }

  @Test
  void asd() {
    PresetRepository presets = app.data().presets();

    Preset preset = Preset.builder()
        .name("asd")
        .build();

    presets.save(preset);

    assertEquals(1, presets.all().size());
  }


  private static class RepositoryStub extends Repository<EntityStub> {
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