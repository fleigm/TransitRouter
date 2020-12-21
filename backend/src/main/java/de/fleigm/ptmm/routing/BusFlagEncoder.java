package de.fleigm.ptmm.routing;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class BusFlagEncoder extends AbstractFlagEncoder {
  protected final Map<String, Integer> trackTypeSpeedMap = new HashMap<>();
  protected final Set<String> badSurfaceSpeedMap = new HashSet<>();
  private boolean speedTwoDirections;
  // This value determines the maximal possible on roads with bad surfaces
  protected int badSurfaceSpeed;

  /**
   * A map which associates string to speed. Get some impression:
   * http://www.itoworld.com/map/124#fullscreen
   * http://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed
   */
  protected final Map<String, Integer> defaultSpeedMap = new HashMap<>();

  public BusFlagEncoder() {
    this(8, 1, 0);
  }

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
    //restrictedValues.add("destination");

    intendedValues.add("yes");
    intendedValues.add("permissive");
    intendedValues.add("designated");

    potentialBarriers.add("gate");
    potentialBarriers.add("lift_gate");
    potentialBarriers.add("kissing_gate");
    potentialBarriers.add("swing_gate");
    potentialBarriers.add("cattle_grid");

    absoluteBarriers.add("fence");
    absoluteBarriers.add("bollard");
    absoluteBarriers.add("stile");
    absoluteBarriers.add("turnstile");
    absoluteBarriers.add("cycle_barrier");
    absoluteBarriers.add("motorcycle_barrier");
    absoluteBarriers.add("block");
    absoluteBarriers.add("sump_buster");

    badSurfaceSpeedMap.add("cobblestone");
    badSurfaceSpeedMap.add("grass_paver");
    badSurfaceSpeedMap.add("gravel");
    badSurfaceSpeedMap.add("sand");
    badSurfaceSpeedMap.add("paving_stones");
    badSurfaceSpeedMap.add("dirt");
    badSurfaceSpeedMap.add("ground");
    badSurfaceSpeedMap.add("grass");
    badSurfaceSpeedMap.add("unpaved");
    badSurfaceSpeedMap.add("compacted");

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

    // disable tracks!
    //defaultSpeedMap.put("track", 15);

    trackTypeSpeedMap.put("grade1", 20); // paved
    trackTypeSpeedMap.put("grade2", 15); // now unpaved - gravel mixed with ...
    trackTypeSpeedMap.put("grade3", 10); // ... hard and soft materials
    trackTypeSpeedMap.put(null, 15);

    // limit speed on bad surfaces to 30 km/h
    badSurfaceSpeed = 30;
    maxPossibleSpeed = 100;
    speedDefault = defaultSpeedMap.get("secondary");
  }

  public BusFlagEncoder setSpeedTwoDirections(boolean value) {
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
    registerNewEncodedValue.add(avgSpeedEnc = new UnsignedDecimalEncodedValue(EncodingManager.getKey(prefix, "average_speed"), speedBits, speedFactor, speedTwoDirections));
  }

  protected double getSpeed(ReaderWay way) {
    String highwayValue = way.getTag("highway");
    if (!Helper.isEmpty(highwayValue) && way.hasTag("motorroad", "yes")
        && !"motorway".equals(highwayValue) && !"motorway_link".equals(highwayValue)) {
      highwayValue = "motorroad";
    }
    Integer speed = defaultSpeedMap.get(highwayValue);
    if (speed == null)
      throw new IllegalStateException(toString() + ", no speed found for: " + highwayValue + ", tags: " + way);

    if (highwayValue.equals("track")) {
      String tt = way.getTag("tracktype");
      if (!Helper.isEmpty(tt)) {
        Integer tInt = trackTypeSpeedMap.get(tt);
        if (tInt != null)
          speed = tInt;
      }
    }

    return speed;
  }

  @Override
  public EncodingManager.Access getAccess(ReaderWay way) {
    String highwayValue = way.getTag("highway");
    String firstValue = way.getFirstPriorityTag(restrictions);

    if (highwayValue == null) {
      return EncodingManager.Access.CAN_SKIP;
    }

    if (!defaultSpeedMap.containsKey(highwayValue)) {
      return EncodingManager.Access.CAN_SKIP;
    }

    if (way.hasTag("impassable", "yes") || way.hasTag("status", "impassable")) {
      return EncodingManager.Access.CAN_SKIP;
    }

    // multiple restrictions needs special handling compared to foot and bike, see also motorcycle
    if (!firstValue.isEmpty()) {
      if (restrictedValues.contains(firstValue) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way))
        return EncodingManager.Access.CAN_SKIP;
      if (intendedValues.contains(firstValue))
        return EncodingManager.Access.WAY;
    }

    // do not drive street cars into fords
    if (isBlockFords() && ("ford".equals(highwayValue) || way.hasTag("ford"))) {
      return EncodingManager.Access.CAN_SKIP;
    }

    if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way)) {
      return EncodingManager.Access.CAN_SKIP;
    } else {
      return EncodingManager.Access.WAY;
    }
  }

  @Override
  public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, EncodingManager.Access accept) {
    if (accept.canSkip() || accept.isFerry()) {
      return edgeFlags;
    }

    // get assumed speed from highway type
    double speed = getSpeed(way);
    speed = applyMaxSpeed(way, speed);

    speed = applyBadSurfaceSpeed(way, speed);

    setSpeed(false, edgeFlags, speed);
    if (speedTwoDirections)
      setSpeed(true, edgeFlags, speed);

    boolean isRoundabout = roundaboutEnc.getBool(false, edgeFlags);
    if (isOneway(way) || isRoundabout) {
      if (isForwardOneway(way))
        accessEnc.setBool(false, edgeFlags, true);
      if (isBackwardOneway(way))
        accessEnc.setBool(true, edgeFlags, true);
    } else {
      accessEnc.setBool(false, edgeFlags, true);
      accessEnc.setBool(true, edgeFlags, true);
    }

    // allow designated ways only if they a part of a bus route
    boolean busRouteNetwork = getBooleanEncodedValue(BusRouteNetwork.KEY).getBool(true, edgeFlags);
    if (way.hasTag("motor_vehicle", "destination") && !busRouteNetwork) {
      accessEnc.setBool(true, edgeFlags, false);
      accessEnc.setBool(false, edgeFlags, false);
    }

    return edgeFlags;
  }

  /**
   * make sure that isOneway is called before
   */
  protected boolean isBackwardOneway(ReaderWay way) {
    return way.hasTag("oneway", "-1")
           || way.hasTag("vehicle:forward", "no")
           || way.hasTag("motor_vehicle:forward", "no")
           || way.hasTag("busway");
  }

  /**
   * make sure that isOneway is called before
   */
  protected boolean isForwardOneway(ReaderWay way) {
    return !way.hasTag("oneway", "-1")
           && !way.hasTag("vehicle:forward", "no")
           && !way.hasTag("motor_vehicle:forward", "no");
  }

  protected boolean isOneway(ReaderWay way) {
    return way.hasTag("oneway", oneways)
           || way.hasTag("vehicle:backward")
           || way.hasTag("vehicle:forward")
           || way.hasTag("motor_vehicle:backward")
           || way.hasTag("motor_vehicle:forward");
  }

  /**
   * @param way   needed to retrieve tags
   * @param speed speed guessed e.g. from the road type or other tags
   * @return The assumed speed
   */
  protected double applyBadSurfaceSpeed(ReaderWay way, double speed) {
    // limit speed if bad surface
    if (badSurfaceSpeed > 0 && speed > badSurfaceSpeed && way.hasTag("surface", badSurfaceSpeedMap))
      speed = badSurfaceSpeed;
    return speed;
  }

  @Override
  public String toString() {
    return "bus";
  }
}
