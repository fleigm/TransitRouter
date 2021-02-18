package de.fleigm.transitrouter.http.search;

import java.util.Objects;
import java.util.function.Function;

public class SearchCriteria {
  public static final String WILDCARD_KEY = "__WILDCARD__";

  private final String key;
  private final Operation operation;
  private final String value;

  public SearchCriteria(String key, Operation operation, String value) {
    this.key = key;
    this.operation = operation;
    this.value = value;
  }

  public static SearchCriteria create(String key, Operation operation, String value) {
    return new SearchCriteria(key, operation, value);
  }

  public static SearchCriteria createWildcard(String value) {
    return new SearchCriteria(WILDCARD_KEY, Operation.NONE, value);
  }

  public String key() {
    return key;
  }

  public Operation operation() {
    return operation;
  }

  public String value() {
    return value;
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

  @Override
  public String toString() {
    return "SearchCriteria{" +
           "key='" + key + '\'' +
           ", operation=" + operation +
           ", value='" + value + '\'' +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SearchCriteria)) return false;
    SearchCriteria that = (SearchCriteria) o;
    return Objects.equals(key, that.key) && operation == that.operation && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, operation, value);
  }
}
