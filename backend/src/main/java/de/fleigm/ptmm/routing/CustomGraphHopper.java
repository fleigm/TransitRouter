package de.fleigm.ptmm.routing;

import com.graphhopper.reader.osm.GraphHopperOSM;

public class CustomGraphHopper extends GraphHopperOSM {

  /*@Override
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
  }*/
}
