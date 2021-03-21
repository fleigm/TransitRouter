package de.fleigm.transitrouter.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fleigm.transitrouter.http.json.ObjectMapperConfiguration;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionsTest {

  @Test
  void only_one_extension_per_type() {
    Extensions extensions = new Extensions();

    TestExtension extension1 = new TestExtension();
    TestExtension extension2 = new TestExtension();
    extensions.add(extension1);
    extensions.add(extension2);

    assertTrue(extensions.has(TestExtension.class));
    assertEquals(1, extensions.unwrap().size());
    assertEquals(extension2, extensions.get(TestExtension.class).get());
  }

  @Test
  void get_or_create() {
    Extensions extensions = new Extensions();
    TestExtension extension = extensions.getOrCreate(TestExtension.class, TestExtension::new);

    assertTrue(extensions.has(TestExtension.class));
    assertEquals(extension, extensions.getOrCreate(TestExtension.class, TestExtension::new));
  }

  @Test
  void get() {
    Extensions extensions = new Extensions();
    assertFalse(extensions.get(TestExtension.class).isPresent());
    extensions.add(new TestExtension());
    assertTrue(extensions.get(TestExtension.class).isPresent());
  }

  @Test
  void serialization() throws JsonProcessingException {
    ObjectMapper objectMapper = ObjectMapperConfiguration.get();
    Extensions extensions = new Extensions().add(new TestExtension());

    String json = objectMapper.writeValueAsString(extensions);
    Extensions extensionsFromJson = objectMapper.readValue(json, Extensions.class);

    assertEquals(
        "{\"de.fleigm.transitrouter.data.ExtensionsTest$TestExtension\":{" +
        "\"@class\":\"de.fleigm.transitrouter.data.ExtensionsTest$TestExtension\",\"name\":\"test\"}}",
        json);

    assertTrue(extensionsFromJson.has(TestExtension.class));
    assertEquals(1, extensions.unwrap().size());
  }

  @Data
  static class TestExtension implements Extension {
    private String name = "test";
  }
}