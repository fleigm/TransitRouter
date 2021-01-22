package de.fleigm.ptmm.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public final class ValidateGtfsFeed {

  private static final List<String> REQUIRED_FILES = List.of(
      "agency.txt",
      "stops.txt",
      "routes.txt",
      "trips.txt",
      "stop_times.txt"
  );

  private static final List<String> ALLOWED_FILES = List.of(
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
  );

  public static boolean validate(Path path) {
    try (ZipFile archive = new ZipFile(path.toFile())) {
      List<String> entries = archive.stream().map(ZipEntry::getName).collect(Collectors.toList());

      if (!ALLOWED_FILES.containsAll(entries)) {
        return false;
      }

      return entries.containsAll(REQUIRED_FILES);
    } catch (IOException e) {
      log.error("GTFS feed validation failed.", e);
      return false;
    }
  }
}
