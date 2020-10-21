package de.fleigm.ptmm.http.sort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SortQuery {
  private final String attribute;
  private final SortOrder order;

  public SortQuery(String attribute, SortOrder order) {
    this.attribute = attribute;
    this.order = order;
  }

  public static SortQuery parse(String query) {
    Pattern pattern = Pattern.compile("(\\w+):(asc|desc);");
    Matcher matcher = pattern.matcher(query + ";");

    if (!matcher.find()) {
      throw new IllegalArgumentException("Could not pars sort query");
    }
    return new SortQuery(
        matcher.group(1).trim(),
        SortOrder.valueOf(matcher.group(2).trim().toUpperCase()));
  }

  public String attribute() {
    return attribute;
  }

  public SortOrder order() {
    return order;
  }

  public static enum SortOrder {
    ASC, DESC
  }
}
