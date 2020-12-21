package de.fleigm.ptmm.routing;

import com.graphhopper.routing.Path;
import com.graphhopper.util.shapes.GHPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class RoutingResult {
  private final Path path;
  private final List<Path> pathSegments;
  private final double distance;
  private final double time;
  private final List<Observation> observations;
  private final List<GHPoint> candidates;
}
