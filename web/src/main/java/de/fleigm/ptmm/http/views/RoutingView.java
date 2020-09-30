package de.fleigm.ptmm.http.views;

import com.conveyal.gtfs.model.Stop;
import com.graphhopper.routing.Path;
import com.vividsolutions.jts.geom.LineString;
import de.fleigm.ptmm.routing.RoutingResult;

import java.util.List;
import java.util.stream.Collectors;

public class RoutingView {

  public final Path shape;
  public final LineString originalShape;
  public final List<TimeStepView> timeSteps;
  public final List<Stop> stops;

  public RoutingView(RoutingResult result, List<Stop> stops, LineString originalShape) {
    this.shape = result.getPath();
    this.timeSteps = result.getTimeSteps()
        .stream()
        .map(TimeStepView::new)
        .collect(Collectors.toList());

    this.stops = stops;
    this.originalShape = originalShape;
  }
}
