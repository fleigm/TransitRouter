package de.fleigm.transitrouter.gtfs;

import de.fleigm.transitrouter.util.Range;

import java.util.Arrays;

/**
 * GTFS vehicle / route type with support for the googles extended route types.
 * Extended route type are matched to their base type e.g. 701 will be mapped to BUS
 */
public enum Type {
  TRAM(0, Range.of(900, 999)),
  SUBWAY(1),
  RAIL(2, Range.of(100, 199), Range.of(400, 499)),
  BUS(3, Range.of(700, 799)),
  Ferry(4),
  CABLE_TRAM(5),
  AERIAL_LIFT(6),
  FUNICULAR(7),
  UNKNOWN(-1);

  private final int value;
  private final Range[] ranges;

  Type(int value, Range... ranges) {
    this.value = value;
    this.ranges = ranges;
  }

  public static Type create(int type) {
    return Arrays.stream(Type.values())
        .filter(t -> t.contains(type))
        .findFirst()
        .orElse(UNKNOWN);
  }

  public int value() {
    return value;
  }

  public Range[] ranges() {
    return ranges;
  }

  public boolean contains(int type) {
    if (value == type) {
      return true;
    }

    // looks nicer but might be slower?
    //return Arrays.stream(ranges).anyMatch(range -> range.contains(type));

    for (Range range : ranges) {
      if (range.contains(type)) {
        return true;
      }
    }

    return false;
  }
}
