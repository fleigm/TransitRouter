package de.fleigm.transitrouter;

import com.conveyal.gtfs.model.ShapePoint;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.DouglasPeucker;
import com.graphhopper.util.PointList;
import de.fleigm.transitrouter.routing.RoutingResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for converting a {@link RoutingResult} into a list of {@link ShapePoint}s.
 */
@Data
@Accessors(fluent = true)
public class Shape {
  private final PointList points;

  /**
   * Create a new Shape from a routing result.
   *
   * @param routingResult routing result.
   * @return shape.
   */
  public static Shape of(RoutingResult routingResult) {
    return new Shape(routingResult.getPath().calcPoints());
  }

  /**
   * Create a new Shape from a list of points.
   *
   * @param points points.
   * @return shape.
   */
  public static Shape of(PointList points) {
    return new Shape(points);
  }

  /**
   * Create a list of {@link ShapePoint} with a given shape id and calculate
   * shape_dist_traveled and shape_pt_sequence.
   * The shape_dist_traveled is calculated in meters.
   *
   * @param shapeId shape id.
   * @return ordered list of shape points.
   */
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

  public void simplify() {
    new DouglasPeucker().simplify(points);
  }

}
