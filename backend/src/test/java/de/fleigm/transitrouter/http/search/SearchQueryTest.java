package de.fleigm.transitrouter.http.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryTest {


  @Test
  void parse() {
    assertTrue(SearchQuery.parse("").isEmpty());
    assertTrue(SearchQuery.parse(";").isEmpty());
    assertTrue(SearchQuery.parse(" ; ").isEmpty());

    assertEquals(SearchCriteria.create("a", Operation.EQUALITY, "b"), SearchQuery.parse("a:b").get("a"));
    assertEquals(SearchCriteria.create("a", Operation.LESS_THAN, "b"), SearchQuery.parse("a<b").get("a"));
    assertEquals(SearchCriteria.create("a", Operation.GREATER_THAN, "b"), SearchQuery.parse("a>b").get("a"));
    assertEquals(SearchCriteria.create("a", Operation.LIKE, "b"), SearchQuery.parse("a~b").get("a"));

    SearchQuery multiplePredicates = SearchQuery.parse("a:1;b:2; c:3");
    assertEquals(SearchCriteria.create("a", Operation.EQUALITY, "1"), multiplePredicates.get("a"));
    assertEquals(SearchCriteria.create("b", Operation.EQUALITY, "2"), multiplePredicates.get("b"));
    assertEquals(SearchCriteria.create("c", Operation.EQUALITY, "3"), multiplePredicates.get("c"));
  }

  @Test
  void wildcard() {
    assertEquals(SearchCriteria.createWildcard("test"), SearchQuery.parse("test").get(SearchCriteria.WILDCARD_KEY));
    assertEquals(SearchCriteria.createWildcard("test"), SearchQuery.parse(" test ").get(SearchCriteria.WILDCARD_KEY));
    assertEquals(SearchCriteria.createWildcard("test:"), SearchQuery.parse("test:").get(SearchCriteria.WILDCARD_KEY));

    SearchQuery multiplePredicates = SearchQuery.parse("a:1;test; c:3");
    assertEquals(SearchCriteria.create("a", Operation.EQUALITY, "1"), multiplePredicates.get("a"));
    assertEquals(SearchCriteria.createWildcard("test"), multiplePredicates.get(SearchCriteria.WILDCARD_KEY));
    assertEquals(SearchCriteria.create("c", Operation.EQUALITY, "3"), multiplePredicates.get("c"));
  }
}