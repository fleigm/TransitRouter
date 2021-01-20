package de.fleigm.ptmm.eval.api;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import de.fleigm.ptmm.routing.DefaultTransitRouter;
import de.fleigm.ptmm.routing.TransitRouter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DefaultTransitRouterFactory implements TransitRouterFactory {

  @Inject
  GraphHopper graphHopper;

  @Override
  public TransitRouter create(PMap parameters) {
    return new DefaultTransitRouter(graphHopper, parameters);
  }
}
