package de.fleigm.transitrouter.util;

import java.util.Objects;

public class Range {
  private final int start;
  private final int end;

  private Range(int start, int end) {
    this.start = start;
    this.end = end;
  }

  public static Range of(int start, int end) {
    return new Range(start, end);
  }

  public boolean contains(int value) {
    return start <= value && value <= end;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Range)) return false;
    Range range = (Range) o;
    return start == range.start && end == range.end;
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }

  @Override
  public String toString() {
    return String.format("Range{%d - %d}", start, end);
  }
}
