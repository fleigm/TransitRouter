package de.fleigm.transitrouter.routing;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;

import java.util.List;

public class FallbackTransitRouter implements TransitRouter {
  private final TransitRouter transitRouter;
  private final TransitRouter fallbackTransitRouter;

  public FallbackTransitRouter(GraphHopper graphHopper, PMap parameters) {
    transitRouter = new DefaultTransitRouter(graphHopper, parameters);
    fallbackTransitRouter = new DefaultTransitRouter(
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
