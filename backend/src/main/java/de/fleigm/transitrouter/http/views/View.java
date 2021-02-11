package de.fleigm.transitrouter.http.views;

import java.util.HashMap;
import java.util.Map;

public class View {
  private final Map<String, Object> bag = new HashMap<>();

  public View add(String key, Object value) {
    bag.put(key, value);
    return this;
  }

  public Map<String, Object> bag() {
    return bag;
  }
}
