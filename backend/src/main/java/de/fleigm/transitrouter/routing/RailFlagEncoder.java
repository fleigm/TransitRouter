package de.fleigm.transitrouter.routing;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.EncodingManager.Access;
import com.graphhopper.routing.util.spatialrules.TransportationMode;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;

import java.util.List;
import java.util.Set;

public class RailFlagEncoder extends AbstractFlagEncoder {

  private static final Set<String> allowedWays = Set.of("tram", "subway", "rail", "light_rail");

  public RailFlagEncoder(PMap properties) {
    super(properties.getInt("speed_bits", 6),
        properties.getDouble("speed_factor", 1),
        properties.getBool("turn_costs", false) ? 1 : 0);

    this.maxPossibleSpeed = 40;
  }

  @Override
  public Access getAccess(ReaderWay way) {
    if (!way.hasTag("railway", allowedWays)) {
      return Access.CAN_SKIP;
    }

    return Access.WAY;
  }

  @Override
  public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, Access access) {
    if (access.canSkip()) {
      return edgeFlags;
    }

    double speed = applyMaxSpeed(way, getSpeed(way));
    setSpeed(false, edgeFlags, speed);

    accessEnc.setBool(false, edgeFlags, true);
    accessEnc.setBool(true, edgeFlags, true);

    return edgeFlags;
  }

  protected double getSpeed(ReaderWay way) {
    return 40;
  }

  /**
   * @param way   needed to retrieve tags
   * @param speed speed guessed e.g. from the road type or other tags
   * @return The assumed speed.
   */
  protected double applyMaxSpeed(ReaderWay way, double speed) {
    double maxSpeed = getMaxSpeed(way);
    if (isValidSpeed(maxSpeed)) {
      // We assume that the average speed is 90% of the allowed maximum
      return maxSpeed * 0.9;
    }
    return speed;
  }

  @Override
  public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
    // first two bits are reserved for route handling in superclass
    super.createEncodedValues(registerNewEncodedValue, prefix, index);
    registerNewEncodedValue.add(avgSpeedEnc = new UnsignedDecimalEncodedValue(EncodingManager.getKey(prefix, "average_speed"), speedBits, speedFactor, false));
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public TransportationMode getTransportationMode() {
    return TransportationMode.MOTOR_VEHICLE;
  }

  @Override
  public String toString() {
    return "rail";
  }
}
