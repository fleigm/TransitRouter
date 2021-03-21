package de.fleigm.transitrouter.http;

import de.fleigm.transitrouter.http.pagination.Page;
import de.fleigm.transitrouter.http.pagination.Pagination;
import de.fleigm.transitrouter.http.search.SearchFilter;
import de.fleigm.transitrouter.http.search.SearchQuery;
import de.fleigm.transitrouter.http.search.StreamSearchHelper;
import de.fleigm.transitrouter.http.sort.SortQuery;
import de.fleigm.transitrouter.http.sort.StreamSortHelper;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourcePageBuilder<T> {
  private final StreamSearchHelper<T> searchHelper = new StreamSearchHelper<>();
  private final StreamSortHelper<T> sortHelper = new StreamSortHelper<>();

  private Pagination pagination;
  private SearchQuery searchQuery;
  private SortQuery sortQuery;
  private URI path;

  public ResourcePageBuilder<T> pagination(Pagination pagination) {
    this.pagination = pagination;
    return this;
  }

  public ResourcePageBuilder<T> searchQuery(SearchQuery searchQuery) {
    this.searchQuery = searchQuery;
    return this;
  }

  public ResourcePageBuilder<T> sortQuery(SortQuery sortQuery) {
    this.sortQuery = sortQuery;
    return this;
  }

  public ResourcePageBuilder<T> path(URI path) {
    this.path = path;
    return this;
  }

  public ResourcePageBuilder<T> addSearch(String key, SearchFilter<T> searchFilter) {
    searchHelper.add(key, searchFilter);
    return this;
  }

  public ResourcePageBuilder<T> addSort(String key, Comparator<T> comparator) {
    sortHelper.add(key, comparator);
    return this;
  }

  public Page<T> build(List<T> resource) {
    return build(resource, Function.identity());
  }

  public <R> Page<R> build(List<T> resource, Function<T, R> mapper) {
    Stream<T> resourceStream = resource.stream();

    if (searchQuery != null) {
      resourceStream = searchHelper.apply(searchQuery, resourceStream);
    }

    if (sortQuery != null) {
      resourceStream = sortHelper.apply(sortQuery, resourceStream);
    }

    List<T> searchedAndSortedResource = resourceStream.collect(Collectors.toList());

    return Page.<R>builder()
        .currentPage(pagination.getPage())
        .perPage(pagination.getLimit())
        .total(searchedAndSortedResource.size())
        .uri(path)
        .data(searchedAndSortedResource.stream()
            .skip(pagination.getOffset())
            .limit(pagination.getLimit())
            .map(mapper)
            .collect(Collectors.toList()))
        .build();
  }
}
