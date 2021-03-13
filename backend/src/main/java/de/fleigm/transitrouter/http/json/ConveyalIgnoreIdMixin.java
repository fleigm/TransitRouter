package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class ConveyalIgnoreIdMixin {
  @JsonIgnore
  abstract String getId();
}
