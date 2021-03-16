package de.fleigm.transitrouter.feeds.evaluation;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public interface MinifyShapevlReport {

  static void minify(Path path) throws IOException {
    List<String> entries = Files.lines(path)
        .map(line -> line.substring(0, StringUtils.ordinalIndexOf(line, "\t", 4)))
        .collect(Collectors.toList());

    Files.write(path, entries);
  }
}
