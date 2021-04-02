package de.fleigm.transitrouter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ValidateGtfsFeed {
  private static final Logger logger = LoggerFactory.getLogger(ValidateGtfsFeed.class);

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
      "stop_features.txt",
      "linked_datasets.txt",
      "routes.txt",
      "route_directions.txt",
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

  /**
   * Check if a given GTFS zip file contains only files defined in the GTFS specification and
   * contains all required files.
   *
   * @param path to GTFS zip file
   * @return if the feed is valid.
   */
  public static boolean validate(Path path) {
    try (ZipFile archive = new ZipFile(path.toFile())) {
      List<String> entries = archive.stream().map(ZipEntry::getName).collect(Collectors.toList());

      if (!ALLOWED_FILES.containsAll(entries)) {
        return false;
      }

      return entries.containsAll(REQUIRED_FILES);
    } catch (IOException e) {
      logger.error("GTFS feed validation failed.", e);
      return false;
    }
  }
}
