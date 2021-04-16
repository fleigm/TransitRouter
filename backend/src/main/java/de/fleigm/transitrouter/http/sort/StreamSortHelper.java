package de.fleigm.transitrouter.http.sort;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Allows to register multiple comparators that will be applied to a stream
 * based on a {@link SortQuery}.
 *
 * @param <T>
 */
public class StreamSortHelper<T> {
  private final Map<String, Comparator<T>> comparators = new HashMap<>();

  /**
   * Add a comparator.
   *
   * @param key        from the sort query
   * @param comparator comparator
   * @return this
   */
  public StreamSortHelper<T> add(String key, Comparator<T> comparator) {
    comparators.put(key, comparator);
    return this;
  }

  /**
   * Sort the stream based on the sort query.
   *
   * @param query  sort query
   * @param stream data stream
   * @return sorted data stream
   */
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
