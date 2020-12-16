package de.fleigm.ptmm.routing;

import java.util.List;

public interface TransitRouter {
  RoutingResult route(List<Observation> observations);
}
