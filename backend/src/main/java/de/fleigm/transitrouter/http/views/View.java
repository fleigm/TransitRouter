package de.fleigm.transitrouter.http.views;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class View {
  private final Map<String, Object> bag = new HashMap<>();

  @JsonAnySetter
  public View add(String key, Object value) {
    bag.put(key, value);
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> bag() {
    return bag;
  }
}
