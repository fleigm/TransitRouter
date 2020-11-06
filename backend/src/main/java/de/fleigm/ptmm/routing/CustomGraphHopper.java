package de.fleigm.ptmm.routing;

import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.DefaultTurnCostProvider;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;

import static com.graphhopper.routing.weighting.TurnCostProvider.NO_TURN_COST_PROVIDER;
import static com.graphhopper.routing.weighting.Weighting.INFINITE_U_TURN_COSTS;

public class CustomGraphHopper extends GraphHopperOSM {

  @Override
  public Weighting createWeighting(Profile profile, PMap hints, boolean disableTurnCosts) {
    if (profile.getWeighting().toLowerCase().equals("custom_shortest")) {
      FlagEncoder encoder = getEncodingManager().getEncoder(profile.getVehicle());
      TurnCostProvider turnCostProvider;
      if (profile.isTurnCosts() && !disableTurnCosts) {
        if (!encoder.supportsTurnCosts())
          throw new IllegalArgumentException("Encoder " + encoder + " does not support turn costs");
        int uTurnCosts = hints.getInt(Parameters.Routing.U_TURN_COSTS, INFINITE_U_TURN_COSTS);
        turnCostProvider = new DefaultTurnCostProvider(encoder, getGraphHopperStorage().getTurnCostStorage(), uTurnCosts);
      } else {
        turnCostProvider = NO_TURN_COST_PROVIDER;
      }

      return new CustomShortestWeighting(encoder, turnCostProvider);
    }
    return super.createWeighting(profile, hints, disableTurnCosts);
  }
}
