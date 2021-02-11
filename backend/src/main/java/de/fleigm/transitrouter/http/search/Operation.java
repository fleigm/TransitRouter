package de.fleigm.transitrouter.http.search;

public enum Operation {
  EQUALITY,
  GREATER_THAN,
  LESS_THAN,
  LIKE;

  public static Operation get(char value) {
    switch (value) {
      case ':':
        return EQUALITY;
      case '<':
        return LESS_THAN;
      case '>':
        return GREATER_THAN;
      case '~':
        return LIKE;
      default:
        return null;
    }
  }
}
