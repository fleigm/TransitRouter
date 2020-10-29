package de.fleigm.ptmm.gtfs;

import java.util.Comparator;
import java.util.Objects;

public class SequenceKey implements Comparable<SequenceKey> {
  public static final Comparator<SequenceKey> COMPARATOR = Comparator.comparing(SequenceKey::id).thenComparingInt(SequenceKey::sequence);

  private final String id;
  private final int sequence;

  private SequenceKey(String id, int sequence) {
    this.id = id;
    this.sequence = sequence;
  }

  public static SequenceKey of(String id, int sequence) {
    return new SequenceKey(id, sequence);
  }

  public String id() {
    return id;
  }

  public int sequence() {
    return sequence;
  }

  @Override
  public int compareTo(SequenceKey key) {
    return COMPARATOR.compare(this, key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SequenceKey)) return false;
    SequenceKey key = (SequenceKey) o;
    return sequence == key.sequence &&
           Objects.equals(id, key.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, sequence);
  }

  @Override
  public String toString() {
    return String.format("Key{id='%s', sequence=%d}", id, sequence);
  }
}
