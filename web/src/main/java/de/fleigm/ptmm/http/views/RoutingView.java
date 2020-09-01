package de.fleigm.ptmm.http.views;

import com.conveyal.gtfs.model.Stop;
import com.graphhopper.util.PointList;
import de.fleigm.ptmm.routing.RoutingResult;

import java.util.List;
import java.util.stream.Collectors;

public class RoutingView {

  private final List<Stop> stops;
  public final PointList route;
  public final List<TimeStepView> timeSteps;


  public RoutingView(RoutingResult result, List<Stop> stops) {
    this.stops = stops;
    this.route = result.getMatchResult().getMergedPath().calcPoints();

    this.timeSteps = result.getTimeSteps()
        .stream()
        .map(TimeStepView::new)
        .collect(Collectors.toList());
  }
}
