package de.fleigm.transitrouter.routing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@Accessors(fluent = true)
class TimeStep {
  private final Observation observation;
  private final List<DirectedCandidate> candidates;
}
