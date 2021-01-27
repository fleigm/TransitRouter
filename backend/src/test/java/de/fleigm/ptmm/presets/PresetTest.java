package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.App;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@QuarkusTest
class PresetTest {

  @Inject
  App app;

  @Test
  void asd() {
    PresetRepository presets = app.data().presets();

    Preset preset = Preset.builder()
        .name("test")
        .createdAt(LocalDateTime.now())
        .build();

    presets.save(preset);

    preset.setName("new name");

    FeedDetails feedDetails = FeedDetails.builder()
        .trips(123)
        .build();

    preset.addExtension(feedDetails);

    presets.save(preset);

    app.shutdown();

    Preset preset1 = app.data().presets().find(preset.getId()).get();

    assertEquals("new name", preset1.getName());
    assertTrue(preset1.hasExtension(FeedDetails.class));

  }
}