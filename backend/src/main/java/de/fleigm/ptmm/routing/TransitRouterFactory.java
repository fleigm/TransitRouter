package de.fleigm.ptmm.routing;

import com.graphhopper.util.PMap;
import de.fleigm.ptmm.eval.Info;

public interface TransitRouterFactory {

  TransitRouter create(PMap parameters);

  default TransitRouter create(Info info, PMap parameters) {
    return create(info.getParameters().toPropertyMap().putAll(parameters));
  }

  default TransitRouter create(Info info) {
    return create(info, new PMap());
  }
}
