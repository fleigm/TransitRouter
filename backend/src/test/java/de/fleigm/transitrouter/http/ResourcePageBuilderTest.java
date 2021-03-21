package de.fleigm.transitrouter.http;

import de.fleigm.transitrouter.http.pagination.Page;
import de.fleigm.transitrouter.http.pagination.Pagination;
import de.fleigm.transitrouter.http.search.SearchCriteria;
import de.fleigm.transitrouter.http.search.SearchQuery;
import de.fleigm.transitrouter.http.sort.SortQuery;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourcePageBuilderTest {

  @Test
  void search() {
    List<String> items = List.of("a1", "b1", "a2", "b2", "a3", "b3", "a4", "b4", "a5", "b5");

    Page<String> allPage = getResourcePageBuilder().build(items);
    assertEquals(10, allPage.getTotal());
    assertEquals(List.of("a1", "b1", "a2", "b2", "a3"), allPage.getData());

    Page<String> aPage = getResourcePageBuilder().searchQuery(SearchQuery.parse("a")).build(items);
    assertEquals(5, aPage.getTotal());
    assertEquals(List.of("a1", "a2", "a3", "a4", "a5"), aPage.getData());

    Page<String> pageWithUnknownSearch = getResourcePageBuilder()
        .searchQuery(SearchQuery.parse("UNKNOWN:a"))
        .build(items);
    assertEquals(10, pageWithUnknownSearch.getTotal());
    assertEquals(List.of("a1", "b1", "a2", "b2", "a3"), pageWithUnknownSearch.getData());
  }

  @Test
  void sort() {
    List<String> items = List.of("aaa", "aaaa", "aa", "a");

    Page<String> ascPage = getResourcePageBuilder().sortQuery(SortQuery.parse("byLength:asc")).build(items);
    assertEquals(List.of("a", "aa", "aaa", "aaaa"), ascPage.getData());

    Page<String> descPage = getResourcePageBuilder().sortQuery(SortQuery.parse("byLength:desc")).build(items);
    assertEquals(List.of("aaaa", "aaa", "aa", "a"), descPage.getData());

    Page<String> unknownSort = getResourcePageBuilder().sortQuery(SortQuery.parse("UNKNOWN:asc")).build(items);
    assertEquals(items, unknownSort.getData());
  }

  @Test
  void mapper() {
    List<String> items = List.of("a", "b", "c");

    Page<String> page = getResourcePageBuilder().build(items, String::toUpperCase);
    assertEquals(List.of("A", "B", "C"), page.getData());
  }

  private ResourcePageBuilder<String> getResourcePageBuilder() {
    return new ResourcePageBuilder<String>()
        .pagination(Pagination.of(1, 5))
        .addSearch(SearchCriteria.WILDCARD_KEY, (query, value) -> value.startsWith(query.value()))
        .addSort("byLength", Comparator.comparing(String::length))
        .path(URI.create("http://example.test"));
  }

}