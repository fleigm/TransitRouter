package de.fleigm.ptmm.util;

import com.graphhopper.matching.Observation;
import com.graphhopper.util.PointList;

import java.util.List;

public abstract class Helper {

  public static PointList toPointList(List<Observation> observations) {
    PointList points = new PointList();
    for (var observation : observations) {
      points.add(observation.getPoint());
    }
    return points;
  }
}
