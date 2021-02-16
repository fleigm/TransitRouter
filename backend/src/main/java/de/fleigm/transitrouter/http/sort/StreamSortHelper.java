package de.fleigm.transitrouter.http.sort;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class StreamSortHelper<T> {
  private Map<String, Comparator<T>> comparators = new HashMap<>();

  public StreamSortHelper<T> add(String key, Comparator<T> comparator) {
    comparators.put(key, comparator);
    return this;
  }

  public Stream<T> apply(SortQuery query, Stream<T> stream) {
    Comparator<T> comparator = comparators.get(query.attribute());

    if (comparator != null) {
      if (query.order() == SortQuery.SortOrder.DESC) {
        comparator = comparator.reversed();
      }
      stream = stream.sorted(comparator);
    }

    return stream;
  }
}
