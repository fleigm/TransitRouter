package de.fleigm.transitrouter.feeds.api;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.routing.GraphHopperMapMatching;
import de.fleigm.transitrouter.routing.SafeTransitRouterMapMatching;
import de.fleigm.transitrouter.routing.TransitRouter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

public interface TransitRouterFactory {

  TransitRouter create(PMap parameters);

  default TransitRouter create(Parameters parameters) {
    return create(parameters.toPropertyMap());
  }

  @ApplicationScoped
  class Default implements TransitRouterFactory {

    GraphHopper graphHopper;

    Default() {
    }

    @Inject
    public Default(GraphHopper graphHopper) {
      this.graphHopper = graphHopper;
    }

    @Override
    public TransitRouter create(PMap parameters) {
      if (parameters.getBool("use_graph_hopper_map_matching", false)) {
        return new GraphHopperMapMatching(graphHopper, parameters);
      }
      return new SafeTransitRouterMapMatching(graphHopper, parameters);
    }
  }
}
