package de.fleigm.transitrouter.http.sort;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sort query for HTTP requests.
 */
@Data
@Accessors(fluent = true)
public class SortQuery {
  private final String attribute;
  private final SortOrder order;

  public static SortQuery of(String attribute, SortOrder order) {
    return new SortQuery(attribute, order);
  }

  /**
   * Creates a {@link SortQuery} from its string representation.
   * The format is [ATTRIBUTE]:asc or [ATTRIBUTE]:desc.
   * If query is null or blank a {@link SortQuery} object with __NONE__:asc is created.
   *
   * @param query query string
   * @return sort query object
   */
  public static SortQuery parseNullable(String query) {
    if (query == null || query.isBlank()) {
      return parse("__NONE__:asc");
    }
    return parse(query);
  }

  /**
   * Creates a {@link SortQuery} from its string representation.
   * The format is [ATTRIBUTE]:asc or [ATTRIBUTE]:desc
   *
   * @param query query string
   * @return sort query object
   */
  public static SortQuery parse(String query) {
    Pattern pattern = Pattern.compile("(\\w+):(asc|desc)");
    Matcher matcher = pattern.matcher(query);

    if (!matcher.find()) {
      throw new IllegalArgumentException("Could not pars sort query");
    }
    return of(
        matcher.group(1).trim(),
        SortOrder.valueOf(matcher.group(2).trim().toUpperCase()));
  }

  public enum SortOrder {
    ASC, DESC
  }
}
