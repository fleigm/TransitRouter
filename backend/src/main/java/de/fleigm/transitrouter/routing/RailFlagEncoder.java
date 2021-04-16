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

/**
 * Flag encoder for rail vehicles like tram, subway, and trains.
 */
public class RailFlagEncoder extends AbstractFlagEncoder {

  private static final Set<String> allowedWays = Set.of("tram", "subway", "rail", "light_rail");

  public RailFlagEncoder(PMap properties) {
    super(properties.getInt("speed_bits", 6),
        properties.getDouble("speed_factor", 1),
        properties.getBool("turn_costs", false) ? 1 : 0);

    this.maxPossibleSpeed = 40;

    blockPrivate(properties.getBool("block_private", true));
    blockFords(properties.getBool("block_fords", false));
    blockBarriersByDefault(properties.getBool("block_barriers", true));
  }

  @Override
  public Access getAccess(ReaderWay way) {
    return way.hasTag("railway", allowedWays) ? Access.WAY : Access.CAN_SKIP;
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

  @Override
  public void createEncodedValues(List<EncodedValue> registerNewEncodedValue,
                                  String prefix,
                                  int index) {

    // first two bits are reserved for route handling in superclass
    super.createEncodedValues(registerNewEncodedValue, prefix, index);

    avgSpeedEnc = new UnsignedDecimalEncodedValue(
        EncodingManager.getKey(prefix, "average_speed"),
        speedBits,
        speedFactor,
        false);

    registerNewEncodedValue.add(avgSpeedEnc);
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
