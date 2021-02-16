package de.fleigm.transitrouter.http.search;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class StreamSearchHelper<T> {
  private Map<String, SearchFilter<T>> searchFilters = new HashMap<>();

  public StreamSearchHelper<T> add(String key, SearchFilter<T> searchFilter) {
    searchFilters.put(key, searchFilter);
    return this;
  }

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
