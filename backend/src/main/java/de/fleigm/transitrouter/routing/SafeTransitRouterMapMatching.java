package de.fleigm.transitrouter.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;

import java.util.List;

/**
 * Implementation of the TransitRouterMapMatching (TRMM) that retries to find a route
 * with turn restrictions disabled if the markov chain is broken.
 */
public class SafeTransitRouterMapMatching implements TransitRouter {
  private final TransitRouter transitRouter;
  private final TransitRouter fallbackTransitRouter;

  public SafeTransitRouterMapMatching(GraphHopper graphHopper, PMap parameters) {
    transitRouter = new TransitRouterMapMatching(graphHopper, parameters);
    fallbackTransitRouter = new TransitRouterMapMatching(
        graphHopper,
        new PMap(parameters).putObject("disable_turn_costs", true));
  }

  @Override
  public RoutingResult route(List<Observation> observations) {
    try {
      return transitRouter.route(observations);
    } catch (BrokenSequenceException e) {
      return fallbackTransitRouter.route(observations);
    }
  }
}
