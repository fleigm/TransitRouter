package de.fleigm.transitrouter.feeds.process;

import de.fleigm.transitrouter.util.ValidateGtfsFeed;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidateGtfsFeedTest {

  private static Stream<List<String>> succeed_if_feed_has_additional_optional_files_provider() {
    return Stream.of(
        List.of(
            "agency.txt",
            "stops.txt",
            "routes.txt",
            "trips.txt",
            "stop_times.txt",
            "calendar.txt",
            "calendar_dates.txt",
            "fare_attributes.txt",
            "fare_rules.txt",
            "shapes.txt",
            "frequencies.txt",
            "transfers.txt",
            "pathways.txt",
            "levels.txt",
            "translations.txt",
            "feed_info.txt",
            "attributions.txt"
        ), List.of(
            "agency.txt",
            "stops.txt",
            "routes.txt",
            "trips.txt",
            "stop_times.txt",
            "calendar.txt",
            "calendar_dates.txt",
            "fare_attributes.txt",
            "fare_rules.txt",
            "shapes.txt",
            "frequencies.txt",
            "transfers.txt",
            "pathways.txt",
            "levels.txt",
            "translations.txt"
        ), List.of(
            "agency.txt",
            "stops.txt",
            "routes.txt",
            "trips.txt",
            "stop_times.txt",
            "calendar.txt",
            "calendar_dates.txt",
            "shapes.txt",
            "frequencies.txt",
            "transfers.txt",
            "pathways.txt",
            "levels.txt",
            "translations.txt",
            "feed_info.txt",
            "attributions.txt"
        )
    );
  }

  @Test
  void succeed_if_all_required_files_are_contained() throws IOException {
    Path path = createFeed("succeed_if_all_required_files_are_contained", List.of(
        "agency.txt",
        "stops.txt",
        "routes.txt",
        "trips.txt",
        "stop_times.txt"));

    assertTrue(ValidateGtfsFeed.validate(path));
  }

  @Test
  void fail_if_required_file_is_missing() throws IOException {
    Path path = createFeed("fail_if_required_file_is_missing", List.of(
        "stops.txt",
        "routes.txt",
        "trips.txt",
        "stop_times.txt"));

    assertFalse(ValidateGtfsFeed.validate(path));
  }

  @Test
  void fail_on_empty_feed() throws IOException {
    Path path = createFeed("fail_on_empty_feed", Collections.emptyList());
    assertFalse(ValidateGtfsFeed.validate(path));
  }

  @ParameterizedTest
  @MethodSource("succeed_if_feed_has_additional_optional_files_provider")
  void succeed_if_feed_has_additional_optional_files(List<String> files) throws IOException {
    Path path = createFeed("succeed_if_feed_has_additional_optional_files", files);

    assertTrue(ValidateGtfsFeed.validate(path));
  }

  @Test
  void fail_on_not_allowed_files() throws IOException {
    Path path = createFeed("succeed_if_all_required_files_are_contained", List.of(
        "agency.txt",
        "stops.txt",
        "routes.txt",
        "trips.txt",
        "stop_times.txt",
        "UNKNOWN_FILE"));

    assertFalse(ValidateGtfsFeed.validate(path));
  }

  private Path createFeed(String name, List<String> files) throws IOException {
    Path path = Files.createTempFile("name", ".zip");

    path.toFile().deleteOnExit();

    try (var archive = new ZipArchiveOutputStream(new FileOutputStream(path.toFile()))) {
      for (var file : files) {
        ArchiveEntry entry = new ZipArchiveEntry(file);
        archive.putArchiveEntry(entry);
        archive.closeArchiveEntry();
      }
    }

    return path;
  }

}