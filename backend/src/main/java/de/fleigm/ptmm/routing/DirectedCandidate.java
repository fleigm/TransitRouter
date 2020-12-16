package de.fleigm.ptmm.routing;

import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@Builder
@AllArgsConstructor
@Accessors(fluent = true)
public class DirectedCandidate {
  private final Observation observation;
  private final Snap snap;
  private final EdgeIteratorState outgoingEdge;
}
