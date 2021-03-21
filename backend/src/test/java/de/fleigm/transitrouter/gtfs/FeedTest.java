package de.fleigm.transitrouter.gtfs;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FeedTest {

  private Path storage;

  @BeforeEach
  void beforeEach() throws IOException {
    storage = Files.createTempDirectory(UUID.randomUUID().toString());
  }

  @Test
  void create_feed_from_input_stream() throws IOException {
    File feedFile = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    Feed feed = Feed.create(storage.resolve(UUID.randomUUID().toString() + ".zip"), FileUtils.openInputStream(feedFile));

    assertTrue(Files.exists(feed.getPath()));
    assertTrue(Files.exists(feed.getFolder()));
    assertTrue(Files.exists(feed.getFolder().resolve("agency.txt")));

  }

  @Test
  void fail_if_feed_already_exists() throws IOException {
    File feedFile = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    Feed feed = Feed.create(storage.resolve(UUID.randomUUID().toString() + ".zip"), FileUtils.openInputStream(feedFile));

    assertThrows(IllegalArgumentException.class,
        () -> Feed.create(feed.getPath(), FileUtils.openInputStream(feedFile)));
  }

  @Test
  void if_not_valid_fail_and_delete_file() throws IOException {
    File feedFile = new File(getClass().getClassLoader().getResource("invalid_feed.zip").getFile());
    Path path = storage.resolve(UUID.randomUUID().toString() + ".zip");

    assertThrows(IllegalArgumentException.class, () -> Feed.create(path, FileUtils.openInputStream(feedFile)));
    assertFalse(Files.exists(path));
  }

  @Test
  void create_from_transit_feed() {
    File feedFile = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());
    TransitFeed transitFeed = new TransitFeed(feedFile.getPath());

    Feed feed = Feed.createFromTransitFeed(storage.resolve(UUID.randomUUID().toString() + ".zip"), transitFeed);

    assertTrue(Files.exists(feed.getPath()));
    assertTrue(Files.exists(feed.getFolder()));
    assertTrue(Files.exists(feed.getFolder().resolve("agency.txt")));
  }

}