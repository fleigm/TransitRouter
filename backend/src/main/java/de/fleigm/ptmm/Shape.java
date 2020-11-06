package de.fleigm.ptmm;

import com.conveyal.gtfs.model.ShapePoint;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.PointList;

import java.util.ArrayList;
import java.util.List;

public class Shape {
  private final PointList points;

  public Shape(PointList points) {
    this.points = points;
  }

  public PointList points() {
    return points;
  }

  public List<ShapePoint> convertToShapePoints(String shapeId) {
    List<ShapePoint> shapePoints = new ArrayList<>(points.getSize());
    DistancePlaneProjection distanceCalc = new DistancePlaneProjection();
    double distance = 0;
    double prevLat = Double.NaN;
    double prevLon = Double.NaN;

    for (int i = 0; i < points.size(); i++) {
      if (i > 0) {
        distance += distanceCalc.calcDist(prevLat, prevLon, points.getLat(i), points.getLon(i));
      }

      prevLat = points.getLat(i);
      prevLon = points.getLon(i);

      shapePoints.add(new ShapePoint(shapeId, points.getLat(i), points.getLon(i), i, distance));
    }

    return shapePoints;
  }
}
