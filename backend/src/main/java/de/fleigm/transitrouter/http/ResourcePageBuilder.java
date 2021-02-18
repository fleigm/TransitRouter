package de.fleigm.transitrouter.http;

import de.fleigm.transitrouter.http.pagination.Page;
import de.fleigm.transitrouter.http.pagination.Paged;
import de.fleigm.transitrouter.http.search.SearchFilter;
import de.fleigm.transitrouter.http.search.SearchQuery;
import de.fleigm.transitrouter.http.search.StreamSearchHelper;
import de.fleigm.transitrouter.http.sort.SortQuery;
import de.fleigm.transitrouter.http.sort.StreamSortHelper;

import javax.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourcePageBuilder<T> {
  private final StreamSearchHelper<T> searchHelper = new StreamSearchHelper<>();
  private final StreamSortHelper<T> sortHelper = new StreamSortHelper<>();

  private Paged pagination;
  private SearchQuery searchQuery;
  private SortQuery sortQuery;
  private UriInfo uriInfo;

  public ResourcePageBuilder<T> pagination(Paged pagination) {
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

  public ResourcePageBuilder<T> uriInfo(UriInfo uriInfo) {
    this.uriInfo = uriInfo;
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
    Stream<T> resourceStream = resource.stream();
    resourceStream = searchHelper.apply(searchQuery, resourceStream);
    resourceStream = sortHelper.apply(sortQuery, resourceStream);

    List<T> searchedAndSortedResource = resourceStream.collect(Collectors.toList());

    return Page.<T>builder()
        .currentPage(pagination.getPage())
        .perPage(pagination.getLimit())
        .total(searchedAndSortedResource.size())
        .uri(uriInfo.getAbsolutePath())
        .data(searchedAndSortedResource.stream()
            .skip(pagination.getOffset())
            .limit(pagination.getLimit())
            .collect(Collectors.toList()))
        .build();
  }

  public <R> Page<R> build(List<T> resource, Function<T, R> mapper) {
    Stream<T> resourceStream = resource.stream();
    resourceStream = searchHelper.apply(searchQuery, resourceStream);
    resourceStream = sortHelper.apply(sortQuery, resourceStream);

    List<T> searchedAndSortedResource = resourceStream.collect(Collectors.toList());

    return Page.<R>builder()
        .currentPage(pagination.getPage())
        .perPage(pagination.getLimit())
        .total(searchedAndSortedResource.size())
        .uri(uriInfo.getAbsolutePath())
        .data(searchedAndSortedResource.stream()
            .skip(pagination.getOffset())
            .limit(pagination.getLimit())
            .map(mapper)
            .collect(Collectors.toList()))
        .build();
  }
}
