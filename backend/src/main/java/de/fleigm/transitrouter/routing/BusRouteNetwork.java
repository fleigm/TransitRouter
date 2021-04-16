package de.fleigm.transitrouter.routing;

import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;

/**
 * Indicates that a way is part of a bus route relation
 */
public class BusRouteNetwork {
  public static final String KEY = "bus_route_network";

  public static SimpleBooleanEncodedValue create() {
    return new SimpleBooleanEncodedValue(KEY);
  }
}
