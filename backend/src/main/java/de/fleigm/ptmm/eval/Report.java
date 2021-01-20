package de.fleigm.ptmm.eval;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Report {

  private final List<Entry> entries;

  public Report(List<Entry> entries) {
    this.entries = entries;
  }

  public static Report read(Path path) {
    return read(path.toString());
  }

  public static Report read(String file) {
    try {
      List<Entry> entries = Files.lines(Path.of(file))
          .map(line -> line.split("\t"))
          .map(values ->
              new Entry(
                  values[0],
                  Double.parseDouble(values[1]),
                  Double.parseDouble(values[2]),
                  Double.parseDouble(values[3])))
          .collect(Collectors.toList());

      return new Report(entries);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Entry> entries() {
    return entries;
  }

  @Data
  @Accessors(fluent = true)
  public static class Entry {
    public final String tripId;
    public final double an;
    public final double al;
    public final double avgFd;
  }
}
