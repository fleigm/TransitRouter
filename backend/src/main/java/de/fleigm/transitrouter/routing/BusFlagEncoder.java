package de.fleigm.transitrouter.routing;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.spatialrules.TransportationMode;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vehicle profile optimized for public transport via bus.
 * This is based on the {@link com.graphhopper.routing.util.CarFlagEncoder} from GraphHopper.
 */
public class BusFlagEncoder extends AbstractFlagEncoder {
  private boolean speedTwoDirections;

  /**
   * A map which associates string to speed. Get some impression:
   * http://www.itoworld.com/map/124#fullscreen
   * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed
   */
  protected final Map<String, Integer> defaultSpeedMap = new HashMap<>();

  public BusFlagEncoder(PMap properties) {
    this(properties.getInt("speed_bits", 8),
        properties.getDouble("speed_factor", 1),
        properties.getBool("turn_costs", false) ? 1 : 0);

    blockPrivate(properties.getBool("block_private", true));
    blockFords(properties.getBool("block_fords", false));
    blockBarriersByDefault(properties.getBool("block_barriers", true));
    setSpeedTwoDirections(properties.getBool("speed_two_directions", false));
  }

  public BusFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
    super(speedBits, speedFactor, maxTurnCosts);
    restrictions.addAll(Arrays.asList("bus", "psv", "motorcar", "motor_vehicle", "vehicle", "access"));

    restrictedValues.add("agricultural");
    restrictedValues.add("forestry");
    restrictedValues.add("no");
    restrictedValues.add("restricted");
    restrictedValues.add("delivery");
    restrictedValues.add("military");
    restrictedValues.add("emergency");
    restrictedValues.add("private");
    restrictedValues.add("customers");

    intendedValues.add("yes");
    intendedValues.add("permissive");
    intendedValues.add("designated");

    potentialBarriers.add("gate");
    potentialBarriers.add("lift_gate");
    potentialBarriers.add("swing_gate");
    potentialBarriers.add("block");

    absoluteBarriers.add("fence");
    absoluteBarriers.add("bollard");
    absoluteBarriers.add("stile");
    absoluteBarriers.add("turnstile");
    absoluteBarriers.add("cycle_barrier");
    absoluteBarriers.add("motorcycle_barrier");
    absoluteBarriers.add("sump_buster");

    // bus lanes
    defaultSpeedMap.put("platform", 50);
    defaultSpeedMap.put("bus_guideway", 50);

    // autobahn
    defaultSpeedMap.put("motorway", 100);
    defaultSpeedMap.put("motorway_link", 70);
    defaultSpeedMap.put("motorroad", 90);
    // bundesstraße
    defaultSpeedMap.put("trunk", 70);
    defaultSpeedMap.put("trunk_link", 65);
    // linking bigger town
    defaultSpeedMap.put("primary", 65);
    defaultSpeedMap.put("primary_link", 60);
    // linking towns + villages
    defaultSpeedMap.put("secondary", 60);
    defaultSpeedMap.put("secondary_link", 50);
    // streets without middle line separation
    defaultSpeedMap.put("tertiary", 50);
    defaultSpeedMap.put("tertiary_link", 40);
    defaultSpeedMap.put("unclassified", 30);
    defaultSpeedMap.put("residential", 30);
    // spielstraße
    defaultSpeedMap.put("living_street", 5);
    defaultSpeedMap.put("service", 20);
    // unknown road
    defaultSpeedMap.put("road", 20);

    maxPossibleSpeed = 100;
    speedDefault = defaultSpeedMap.get("secondary");
  }

  private BusFlagEncoder setSpeedTwoDirections(boolean value) {
    speedTwoDirections = value;
    return this;
  }

  public TransportationMode getTransportationMode() {
    return TransportationMode.MOTOR_VEHICLE;
  }

  @Override
  public int getVersion() {
    return 2;
  }

  /**
   * Define the place of the speedBits in the edge flags for car.
   */
  @Override
  public void createEncodedValues(List<EncodedValue> registerNewEncodedValue, String prefix, int index) {
    // first two bits are reserved for route handling in superclass
    super.createEncodedValues(registerNewEncodedValue, prefix, index);

    avgSpeedEnc = new UnsignedDecimalEncodedValue(
        EncodingManager.getKey(prefix, "average_speed"),
        speedBits,
        speedFactor,
        speedTwoDirections);

    registerNewEncodedValue.add(avgSpeedEnc);
  }

  protected double getSpeed(ReaderWay way) {
    String highwayValue = way.getTag("highway");
    if (!Helper.isEmpty(highwayValue) && way.hasTag("motorroad", "yes")
        && !"motorway".equals(highwayValue) && !"motorway_link".equals(highwayValue)) {
      highwayValue = "motorroad";
    }

    Integer speed = defaultSpeedMap.get(highwayValue);
    if (speed == null) {
      throw new IllegalStateException(toString() + ", no speed found for: " + highwayValue + ", tags: " + way);
    }

    return speed;
  }

  @Override
  public EncodingManager.Access getAccess(ReaderWay way) {
    String highwayValue = way.getTag("highway");
    String firstValue = way.getFirstPriorityTag(restrictions);

    if (highwayValue == null || !defaultSpeedMap.containsKey(highwayValue)) {
      return EncodingManager.Access.CAN_SKIP;
    }

    if (!firstValue.isEmpty() && restrictedValues.contains(firstValue)) {
      return EncodingManager.Access.CAN_SKIP;
    }

    return EncodingManager.Access.WAY;
  }

  @Override
  public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access accept) {
    if (accept.canSkip() || accept.isFerry()) {
      return edgeFlags;
    }

    applySpeedToWay(edgeFlags, way);
    applyAccessToWay(edgeFlags, way);

    return edgeFlags;
  }

  private void applyAccessToWay(IntsRef edgeFlags, ReaderWay way) {
    boolean isRoundabout = roundaboutEnc.getBool(false, edgeFlags);
    if (isOneway(way) || isRoundabout) {
      if (isForwardOneway(way)) {
        accessEnc.setBool(false, edgeFlags, true);
      }
      if (isBackwardOneway(way)) {
        accessEnc.setBool(true, edgeFlags, true);
      }
    } else {
      accessEnc.setBool(false, edgeFlags, true);
      accessEnc.setBool(true, edgeFlags, true);
    }

    // allow designated ways only if they a part of a bus route
    boolean busRouteNetwork = getBooleanEncodedValue(BusRouteNetwork.KEY).getBool(true, edgeFlags);
    if (way.hasTag(List.of("vehicle", "motor_vehicle"), List.of("destination")) && !busRouteNetwork) {
      accessEnc.setBool(true, edgeFlags, false);
      accessEnc.setBool(false, edgeFlags, false);
    }
  }

  private void applySpeedToWay(IntsRef edgeFlags, ReaderWay way) {
    double speed = applyMaxSpeed(way, getSpeed(way));

    setSpeed(false, edgeFlags, speed);
    if (speedTwoDirections) {
      setSpeed(true, edgeFlags, speed);
    }
  }

  /**
   * make sure that isOneway is called before
   */
  protected boolean isBackwardOneway(ReaderWay way) {
    return way.hasTag("oneway", "-1")
           || way.hasTag("vehicle:forward", "no")
           || way.hasTag("motor_vehicle:forward", "no")
           || way.hasTag("busway")
           || way.hasTag("oneway:psv", "no")
           || way.hasTag("oneway:bus", "no");
  }

  /**
   * make sure that isOneway is called before
   */
  protected boolean isForwardOneway(ReaderWay way) {
    return !way.hasTag("oneway", "-1")
           && !way.hasTag("vehicle:forward", "no")
           && !way.hasTag("motor_vehicle:forward", "no")
           || way.hasTag("busway")
           || way.hasTag("bus:forward", "yes", "designated")
           || way.hasTag("oneway:psv", "no")
           || way.hasTag("oneway:bus", "no");
  }

  protected boolean isOneway(ReaderWay way) {
    return way.hasTag("oneway", oneways)
           || way.hasTag("vehicle:backward")
           || way.hasTag("vehicle:forward")
           || way.hasTag("motor_vehicle:backward")
           || way.hasTag("motor_vehicle:forward");
  }

  @Override
  public String toString() {
    return "bus";
  }
}
