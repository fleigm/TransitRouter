package de.fleigm.transitrouter.http.search;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchQuery {
  public static final Pattern SEARCH_CRITERIA_PATTERN = Pattern.compile("(\\w+|\\w+\\.\\w+|\\w+\\.\\w+\\.\\w+)([:~<>])(.+)");

  private final Map<String, SearchCriteria> criteria;

  private SearchQuery() {
    this(new HashMap<>());
  }

  public SearchQuery(Map<String, SearchCriteria> criteria) {
    this.criteria = criteria;
  }

  public boolean has(String key) {
    return criteria.containsKey(key);
  }

  public SearchCriteria get(String key) {
    return criteria.get(key);
  }

  public SearchQuery add(String key, Operation operation, String value) {
    return add(new SearchCriteria(key, operation, value));
  }

  public SearchQuery add(SearchCriteria criteria) {
    this.criteria.put(criteria.key(), criteria);
    return this;
  }

  public boolean isEmpty() {
    return this.criteria.isEmpty();
  }

  public int size() {
    return this.criteria.size();
  }

  public static SearchQuery parse(String search) {
    SearchQuery searchQuery = new SearchQuery();

    for (String searchEntry : search.split(";")) {
      searchEntry = searchEntry.trim();
      if (!searchEntry.isBlank()) {
        searchQuery.add(parseSearchCriteria(searchEntry));
      }
    }

    return searchQuery;
  }

  private static SearchCriteria parseSearchCriteria(String search) {
    Matcher matcher = SEARCH_CRITERIA_PATTERN.matcher(search);

    if (matcher.find()) {
      String key = matcher.group(1).trim();
      Operation operation = Operation.get(matcher.group(2).charAt(0));
      String value = matcher.group(3).trim();

      return new SearchCriteria(key, operation, value);
    }

    return SearchCriteria.create(SearchCriteria.WILDCARD_KEY, Operation.NONE, search);
  }
}
