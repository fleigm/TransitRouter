package de.fleigm.ptmm;

import com.conveyal.gtfs.model.ShapePoint;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.PointList;
import de.fleigm.ptmm.routing.RoutingResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)
public class Shape {
  private final PointList points;

  public static Shape of(RoutingResult routingResult) {
    return new Shape(routingResult.getPath().calcPoints());
  }

  public static Shape of(PointList points) {
    return new Shape(points);
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
