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

/**
 * Helper class for creating paginated results with search and sort functionality.
 * Search and sort filters are automatically applied on the data
 * based on the search and sort queries.
 *
 * @param <T>
 */
public class ResourcePageBuilder<T> {
  private final StreamSearchHelper<T> searchHelper = new StreamSearchHelper<>();
  private final StreamSortHelper<T> sortHelper = new StreamSortHelper<>();

  private Pagination pagination;
  private SearchQuery searchQuery;
  private SortQuery sortQuery;
  private URI path;

  /**
   * Add pagination.
   *
   * @return this
   */
  public ResourcePageBuilder<T> pagination(Pagination pagination) {
    this.pagination = pagination;
    return this;
  }

  /**
   * Add a search query that will be used to filter the data.
   *
   * @return this
   */
  public ResourcePageBuilder<T> searchQuery(SearchQuery searchQuery) {
    this.searchQuery = searchQuery;
    return this;
  }

  /**
   * Add a sort query that will be used to sort the data.
   *
   * @return this
   */
  public ResourcePageBuilder<T> sortQuery(SortQuery sortQuery) {
    this.sortQuery = sortQuery;
    return this;
  }

  /**
   * Add the URI that will be used for the page links.
   *
   * @return this
   */
  public ResourcePageBuilder<T> path(URI path) {
    this.path = path;
    return this;
  }

  /**
   * Add a search function that can be applied to filter the results.
   * The filter will be applied if its key is in the search query.
   *
   * @param key          filter key
   * @param searchFilter filter function
   * @return this
   */
  public ResourcePageBuilder<T> addSearch(String key, SearchFilter<T> searchFilter) {
    searchHelper.add(key, searchFilter);
    return this;
  }

  /**
   * Add a comparator that can be applied to sort the results
   * The comparator will be applied if its key is in the sort query.
   *
   * @param key        sort key
   * @param comparator comparator
   * @return this
   */
  public ResourcePageBuilder<T> addSort(String key, Comparator<T> comparator) {
    sortHelper.add(key, comparator);
    return this;
  }

  /**
   * Apply search and sort filters to the data and create the requested page.
   *
   * @return paginated data
   */
  public Page<T> build(List<T> data) {
    return build(data, Function.identity());
  }

  /**
   * Apply search and sort filters to the data and create the requested page with the mapped data.
   *
   * @return paginated data.
   */
  public <R> Page<R> build(List<T> data, Function<T, R> mapper) {
    Stream<T> dataStream = data.stream();

    if (searchQuery != null) {
      dataStream = searchHelper.apply(searchQuery, dataStream);
    }

    if (sortQuery != null) {
      dataStream = sortHelper.apply(sortQuery, dataStream);
    }

    List<T> searchedAndSortedResource = dataStream.collect(Collectors.toList());

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
