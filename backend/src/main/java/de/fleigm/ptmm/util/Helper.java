package de.fleigm.ptmm.util;

import com.graphhopper.util.PointList;
import de.fleigm.ptmm.routing.Observation;

import java.util.List;

public abstract class Helper {

  public static PointList toPointList(List<Observation> observations) {
    PointList points = new PointList();
    for (var observation : observations) {
      points.add(observation.point());
    }
    return points;
  }
}
