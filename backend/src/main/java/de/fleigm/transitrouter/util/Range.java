package de.fleigm.transitrouter.util;

import java.util.Objects;

/**
 * Represents a integer range with inclusive start and inclusive end value.
 */
public class Range {
  private final int start;
  private final int end;

  private Range(int start, int end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Create a new integer range
   * @param start inclusive start value
   * @param end inclusive end value
   * @return range
   */
  public static Range of(int start, int end) {
    return new Range(start, end);
  }

  /**
   * Check if a given value is in the range i.e. between the start and end value.
   */
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
