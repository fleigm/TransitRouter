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

  private final Paged pagination;
  private final SearchQuery searchQuery;
  private final SortQuery sortQuery;
  private final UriInfo uriInfo;

  public ResourcePageBuilder(Paged pagination,
                             SearchQuery searchQuery,
                             SortQuery sortQuery,
                             UriInfo uriInfo) {
    this.pagination = pagination;
    this.searchQuery = searchQuery;
    this.sortQuery = sortQuery;
    this.uriInfo = uriInfo;
  }

  public ResourcePageBuilder<T> add(String key, SearchFilter<T> searchFilter) {
    searchHelper.add(key, searchFilter);
    return this;
  }

  public ResourcePageBuilder<T> add(String key, Comparator<T> comparator) {
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
