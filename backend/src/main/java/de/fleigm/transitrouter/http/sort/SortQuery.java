package de.fleigm.transitrouter.http.sort;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SortQuery {
  private final String attribute;
  private final SortOrder order;

  private SortQuery(String attribute, SortOrder order) {
    this.attribute = attribute;
    this.order = order;
  }

  public static SortQuery of(String attribute, SortOrder order) {
    return new SortQuery(attribute, order);
  }

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

  public String attribute() {
    return attribute;
  }

  public SortOrder order() {
    return order;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SortQuery)) return false;
    SortQuery sortQuery = (SortQuery) o;
    return Objects.equals(attribute, sortQuery.attribute) && order == sortQuery.order;
  }

  @Override
  public int hashCode() {
    return Objects.hash(attribute, order);
  }

  public static enum SortOrder {
    ASC, DESC
  }
}
