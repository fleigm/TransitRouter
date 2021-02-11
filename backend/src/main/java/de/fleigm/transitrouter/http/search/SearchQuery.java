package de.fleigm.transitrouter.http.search;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchQuery {
  private Map<String, SearchCriteria> criteria;

  public SearchQuery(Map<String, SearchCriteria> criteria) {
    this.criteria = criteria;
  }

  public boolean has(String key) {
    return criteria.containsKey(key);
  }

  public SearchCriteria get(String key) {
    return criteria.get(key);
  }

  public void computeIfPresent(String key, Consumer<SearchCriteria> func) {
    SearchCriteria searchCriteria = get(key);
    if (searchCriteria != null) {
      func.accept(searchCriteria);
    }
  }

  public SearchQuery only(String... keys) {
    Map<String, SearchCriteria> criteria = new HashMap<>();

    for (String key : keys) {
      this.criteria.computeIfPresent(key, (s, searchCriteria) -> criteria.put(key, searchCriteria));
    }

    this.criteria = criteria;

    return this;
  }

  public SearchQuery reject(String... keys) {
    for (String key : keys) {
      criteria.remove(key);
    }

    return this;
  }

  public SearchQuery add(String key, Operation operation, String value) {
    return add(new SearchCriteria(key, operation, value));
  }

  public SearchQuery add(SearchCriteria criteria) {
    this.criteria.put(criteria.key(), criteria);
    return this;
  }

  public static SearchQuery parse(String search) {
    Map<String, SearchCriteria> criteria = new HashMap<>();

    Pattern pattern = Pattern.compile("(\\w+|\\w+\\.\\w+|\\w+\\.\\w+\\.\\w+)(:|~|<|>)(.+?);");
    Matcher matcher = pattern.matcher(search + ";");

    while (matcher.find()) {
      String key = matcher.group(1).trim();
      Operation operation = Operation.get(matcher.group(2).charAt(0));
      String value = matcher.group(3).trim();

      criteria.put(key, new SearchCriteria(key, operation, value));
    }

    return new SearchQuery(criteria);

  }
}
