package de.fleigm.ptmm.util;

import com.graphhopper.util.PointList;
import de.fleigm.ptmm.routing.Observation;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Helper {

  /**
   * Convert a given list of {@link Observation}s into a {@link PointList}.
   *
   * @param observations observations.
   * @return pointlist of observations.
   */
  public static PointList toPointList(List<Observation> observations) {
    PointList points = new PointList();
    for (var observation : observations) {
      points.add(observation.point());
    }
    return points;
  }

  /**
   * Add files as a zip archive to a given {@link OutputStream}.
   * This is used to allow streaming download.
   *
   * @param output    output stream
   * @param filePaths files to add
   * @throws IOException if something goes wrong.
   */
  public static void buildZipFile(OutputStream output, List<Path> filePaths) throws IOException {
    try (var archive = new ZipArchiveOutputStream(output)) {
      List<File> files = filePaths.stream().map(Path::toFile).collect(Collectors.toList());
      for (File file : files) {
        ArchiveEntry entry = new ZipArchiveEntry(file, file.getName());
        archive.putArchiveEntry(entry);
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
          IOUtils.copy(inputStream, archive);
        }
        archive.closeArchiveEntry();
      }
    }
  }
}
