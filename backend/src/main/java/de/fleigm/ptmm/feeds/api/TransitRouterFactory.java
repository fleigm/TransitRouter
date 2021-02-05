package de.fleigm.ptmm.feeds.api;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import de.fleigm.ptmm.feeds.GeneratedFeed;
import de.fleigm.ptmm.feeds.Parameters;
import de.fleigm.ptmm.routing.DefaultTransitRouter;
import de.fleigm.ptmm.routing.GraphHopperTransitRouter;
import de.fleigm.ptmm.routing.TransitRouter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

public interface TransitRouterFactory {

  TransitRouter create(PMap parameters);

  default TransitRouter create(Parameters parameters) {
    return create(parameters.toPropertyMap());
  }

  default TransitRouter create(GeneratedFeed info, PMap parameters) {
    return create(info.getParameters().toPropertyMap().putAll(parameters));
  }

  default TransitRouter create(GeneratedFeed info) {
    return create(info, new PMap());
  }

  @ApplicationScoped
  class Default implements TransitRouterFactory {

    @Inject
    GraphHopper graphHopper;

    @Override
    public TransitRouter create(PMap parameters) {
      if (parameters.getBool("use_graph_hopper_map_matching", false)) {
        return new GraphHopperTransitRouter(graphHopper, parameters);
      }
      return new DefaultTransitRouter(graphHopper, parameters);
    }
  }
}
