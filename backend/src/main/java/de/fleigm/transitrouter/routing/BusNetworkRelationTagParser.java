package de.fleigm.transitrouter.routing;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EncodedValue;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.util.parsers.RelationTagParser;
import com.graphhopper.storage.IntsRef;

import java.util.List;

public class BusNetworkRelationTagParser implements RelationTagParser {

  private final SimpleBooleanEncodedValue transformerRouteRelEnc;
  private final SimpleBooleanEncodedValue busRouteNetworkEncoding;

  public BusNetworkRelationTagParser() {
    transformerRouteRelEnc = new SimpleBooleanEncodedValue("bus_route_network_relation", true);
    busRouteNetworkEncoding = BusRouteNetwork.create();
  }

  @Override
  public void createRelationEncodedValues(EncodedValueLookup lookup, List<EncodedValue> registerNewEncodedValue) {
    registerNewEncodedValue.add(transformerRouteRelEnc);
  }

  @Override
  public IntsRef handleRelationTags(IntsRef relFlags, ReaderRelation relation) {
    if (relation.hasTag("route", "bus")) {
      transformerRouteRelEnc.setBool(true, relFlags, true);
    }

    return relFlags;
  }

  @Override
  public void createEncodedValues(EncodedValueLookup lookup, List<EncodedValue> registerNewEncodedValue) {
    registerNewEncodedValue.add(busRouteNetworkEncoding);
  }

  @Override
  public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, boolean ferry, IntsRef relationFlags) {
    boolean value = transformerRouteRelEnc.getBool(true, relationFlags);
    busRouteNetworkEncoding.setBool(true, edgeFlags, value);
    return edgeFlags;
  }
}
