package de.fleigm.transitrouter.routing;

import java.util.List;

/**
 *
 */
public interface TransitRouter {

  /**
   * Find the most likely route given a list of observations.
   *
   * @param observations observations.
   * @return routing result.
   */
  RoutingResult route(List<Observation> observations);
}
