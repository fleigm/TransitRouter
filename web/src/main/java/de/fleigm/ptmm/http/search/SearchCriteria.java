package de.fleigm.ptmm.http.search;

public class SearchCriteria {
  private final String key;
  private final Operation operation;
  private final String value;

  public SearchCriteria(String key, Operation operation, String value) {
    this.key = key;
    this.operation = operation;
    this.value = value;
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
}
