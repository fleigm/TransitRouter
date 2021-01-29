package de.fleigm.ptmm.feeds;

import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.util.Unzip;
import de.fleigm.ptmm.util.ValidateGtfsFeed;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class Feed {
  private final Path path;

  @JsonbCreator
  public Feed(@JsonbProperty("path") Path path) {
    this.path = path;
  }

  public static Feed create(Path path, InputStream feed) {
    try {
      if (Files.exists(path)) {
        throw new IllegalArgumentException(String.format("Feed %s already exists.", path));
      }

      FileUtils.copyInputStreamToFile(feed, path.toFile());

      if (!ValidateGtfsFeed.validate(path)) {
        FileUtils.deleteDirectory(path.toFile());
        throw new IllegalArgumentException("File is not a valid GTFS feed.");
      }
      Unzip.apply(path, Path.of(FilenameUtils.removeExtension(path.toString())));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new Feed(path);
  }

  public static Feed createFromTransitFeed(Path path, TransitFeed transitFeed) {
    try {
      transitFeed.internal().toFile(path.toString());
      Unzip.apply(path, Path.of(FilenameUtils.removeExtension(path.toString())));
      return new Feed(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Feed load(Path path) {
    return new Feed(path);
  }

  public Path getFolder() {
    return Path.of(FilenameUtils.removeExtension(path.toString()));
  }
}
