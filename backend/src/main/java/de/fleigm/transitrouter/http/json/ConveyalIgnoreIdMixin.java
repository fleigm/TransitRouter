package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Conveyal GTFS classes have a internal id field that cannot be serialized
 * and needs to be ignored.
 */
public abstract class ConveyalIgnoreIdMixin {
  @JsonIgnore
  abstract String getId();
}
