package de.fleigm.ptmm.routing;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.util.EdgeIteratorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomShortestWeighting extends ShortestWeighting {
  private static final Logger LOGGER = LoggerFactory.getLogger(CustomShortestWeighting.class);

  public CustomShortestWeighting(FlagEncoder flagEncoder) {
    super(flagEncoder);
  }

  public CustomShortestWeighting(FlagEncoder flagEncoder, TurnCostProvider turnCostProvider) {
    super(flagEncoder, turnCostProvider);
  }

  @Override
  public long calcEdgeMillis(EdgeIteratorState edgeState, boolean reverse) {
    double speed = reverse ? edgeState.getReverse(avSpeedEnc) : edgeState.get(avSpeedEnc);

    if (speed == 0) {
      LOGGER.warn("Invalid speed for edge {}. Use 1km/h as speed for time calculation.", edgeState.getEdge());
      return (long) (edgeState.getDistance() * 3600);
    }

    return super.calcEdgeMillis(edgeState, reverse);
  }

  @Override
  public String getName() {
    return "custom_shortest";
  }
}
