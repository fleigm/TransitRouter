package de.fleigm.ptmm.routing;

import com.graphhopper.matching.Observation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@Accessors(fluent = true)
public class ObservationWithCandidates {
  private final Observation observation;
  private final List<DirectedCandidate> candidates;
}
