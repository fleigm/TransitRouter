package de.fleigm.transitrouter.http.search;

/**
 * Check if an object matches a {@link SearchCriteria}.
 *
 * @param <T>
 */
@FunctionalInterface
public interface SearchFilter<T> {

  /**
   * Check if the object matches the {@link SearchCriteria}.
   */
  boolean apply(SearchCriteria searchCriteria, T object);
}
