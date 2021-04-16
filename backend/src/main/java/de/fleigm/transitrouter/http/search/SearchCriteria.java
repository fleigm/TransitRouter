package de.fleigm.transitrouter.http.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.Function;

/**
 * Represents a single entry of a {@link SearchQuery}.
 * A {@link SearchCriteria} consists of a attribute key, a search operation and a search value.
 * For we do not care about the key i.e. we want a wildcard search we is the key __WILDCARD__
 */
@Data
@Accessors(fluent = true)
public class SearchCriteria {
  public static final String WILDCARD_KEY = "__WILDCARD__";

  private final String key;
  private final Operation operation;
  private final String value;

  public static SearchCriteria create(String key, Operation operation, String value) {
    return new SearchCriteria(key, operation, value);
  }

  /**
   * Create a wildcard {@link SearchCriteria} with key __WILDCARD__ and {@link Operation#NONE}
   *
   * @param value search value
   * @return SearchCriteria
   */
  public static SearchCriteria createWildcard(String value) {
    return new SearchCriteria(WILDCARD_KEY, Operation.NONE, value);
  }

  public <T> T valueAs(Function<String, T> mapper) {
    return mapper.apply(value);
  }

  public int intValue() {
    return Integer.parseInt(value);
  }

  public boolean booleanValue() {
    return Boolean.parseBoolean(value);
  }
}
