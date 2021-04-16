package de.fleigm.transitrouter.http.search;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Allows to register multiple search filters that will be applied to a stream
 * based on SearchQuery.
 *
 * @param <T>
 */
public class StreamSearchHelper<T> {
  private final Map<String, SearchFilter<T>> searchFilters = new HashMap<>();

  public StreamSearchHelper<T> add(String key, SearchFilter<T> searchFilter) {
    searchFilters.put(key, searchFilter);
    return this;
  }

  /**
   * Filter the stream based on the SearchQuery and added SearchFilters.
   *
   * @param query  search query
   * @param stream data stream
   * @return filtered data stream
   */
  public Stream<T> apply(SearchQuery query, Stream<T> stream) {
    for (Map.Entry<String, SearchFilter<T>> entry : searchFilters.entrySet()) {
      String key = entry.getKey();
      SearchFilter<T> filter = entry.getValue();

      if (query.has(key)) {
        stream = stream.filter(t -> filter.apply(query.get(key), t));
      }
    }

    return stream;
  }

}
