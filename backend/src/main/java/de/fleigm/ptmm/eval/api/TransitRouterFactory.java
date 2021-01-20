package de.fleigm.ptmm.eval.api;

import com.graphhopper.util.PMap;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.routing.TransitRouter;

public interface TransitRouterFactory {

  TransitRouter create(PMap parameters);

  default TransitRouter create(GeneratedFeedInfo info, PMap parameters) {
    return create(info.getParameters().toPropertyMap().putAll(parameters));
  }

  default TransitRouter create(GeneratedFeedInfo info) {
    return create(info, new PMap());
  }
}
