package de.fleigm.transitrouter.http.search;

@FunctionalInterface
public interface SearchFilter<T> {

  boolean apply(SearchCriteria searchCriteria, T object);
}
