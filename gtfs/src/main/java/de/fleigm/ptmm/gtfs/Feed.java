package de.fleigm.ptmm.gtfs;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class Feed {
  private Map<String, Route> routes;
  private Map<String, Trip> trips;
  private Map<String, Stop> stops;
  private NavigableMap<SequenceKey, ShapePoint> shapePoints;
  private NavigableMap<SequenceKey, StopTime> stopTimes;

  public Optional<Route> getRoute(String id) {
    return Optional.ofNullable(routes.get(id));
  }

  public List<Route> getBusRoutes() {
    return routes.values().stream()
        .filter(route -> route.getType() == 3)
        .collect(Collectors.toList());
  }
}
