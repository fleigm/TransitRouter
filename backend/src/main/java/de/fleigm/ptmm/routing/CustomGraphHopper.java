package de.fleigm.ptmm.routing;

import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.DefaultWeightingFactory;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.DefaultTurnCostProvider;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters;

public class CustomGraphHopper extends GraphHopperOSM {

  @Override
  protected WeightingFactory createWeightingFactory() {
    return new CustomWeightingFactory(getGraphHopperStorage(), getEncodingManager());
  }

  private static class CustomWeightingFactory implements WeightingFactory {
    private final GraphHopperStorage ghStorage;
    private final EncodingManager encodingManager;
    private final WeightingFactory defaultWeightingFactory;

    public CustomWeightingFactory(GraphHopperStorage ghStorage, EncodingManager encodingManager) {
      this.ghStorage = ghStorage;
      this.encodingManager = encodingManager;
      this.defaultWeightingFactory = new DefaultWeightingFactory(ghStorage, encodingManager);
    }

    @Override
    public Weighting createWeighting(Profile profile, PMap hints, boolean disableTurnCosts) {
      if (profile.getWeighting().toLowerCase().equals("custom_shortest")) {
        FlagEncoder encoder = encodingManager.getEncoder(profile.getVehicle());
        TurnCostProvider turnCostProvider;
        if (profile.isTurnCosts() && !disableTurnCosts) {
          if (!encoder.supportsTurnCosts())
            throw new IllegalArgumentException("Encoder " + encoder + " does not support turn costs");
          int uTurnCosts = hints.getInt(Parameters.Routing.U_TURN_COSTS, Weighting.INFINITE_U_TURN_COSTS);
          turnCostProvider = new DefaultTurnCostProvider(encoder, ghStorage.getTurnCostStorage(), uTurnCosts);
        } else {
          turnCostProvider = TurnCostProvider.NO_TURN_COST_PROVIDER;
        }
        return new CustomShortestWeighting(encoder, turnCostProvider);
      }

      return defaultWeightingFactory.createWeighting(profile, hints, disableTurnCosts);
    }
  }
}
