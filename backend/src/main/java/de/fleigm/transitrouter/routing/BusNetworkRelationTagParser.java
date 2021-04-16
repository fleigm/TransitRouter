package de.fleigm.transitrouter.routing;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.util.parsers.RelationTagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

/**
 * Add bus route network encoding to ways based on route relation.
 */
public class BusNetworkRelationTagParser implements RelationTagParser {

  private final SimpleBooleanEncodedValue routeTransformerEncoding;
  private final SimpleBooleanEncodedValue busRouteEncoding;

  public BusNetworkRelationTagParser() {
    routeTransformerEncoding = new SimpleBooleanEncodedValue("bus_route_relation");
    busRouteEncoding = BusRouteNetwork.create();
  }

  @Override
  public void createRelationEncodedValues(EncodedValueLookup lookup,
                                          List<EncodedValue> registerNewEncodedValue) {
    registerNewEncodedValue.add(routeTransformerEncoding);
  }

  @Override
  public IntsRef handleRelationTags(IntsRef relFlags, ReaderRelation relation) {
    routeTransformerEncoding.setBool(false, relFlags, relation.hasTag("route", "bus"));
    return relFlags;
  }

  @Override
  public void createEncodedValues(EncodedValueLookup lookup,
                                  List<EncodedValue> registerNewEncodedValue) {
    registerNewEncodedValue.add(busRouteEncoding);
  }

  @Override
  public IntsRef handleWayTags(IntsRef edgeFlags,
                               ReaderWay way,
                               boolean ferry,
                               IntsRef relationFlags) {
    boolean value = routeTransformerEncoding.getBool(false, relationFlags);
    busRouteEncoding.setBool(false, edgeFlags, value);
    return edgeFlags;
  }
}
