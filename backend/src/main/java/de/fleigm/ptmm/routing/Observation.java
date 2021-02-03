package de.fleigm.ptmm.routing;

import com.graphhopper.util.shapes.GHPoint;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 */
@ToString
@EqualsAndHashCode
public class Observation {
  private final GHPoint point;

  public Observation(GHPoint point) {
    this.point = point;
  }

  public static Observation of(GHPoint point) {
    return new Observation(point);
  }

  public static Observation of(double latitude, double longitude) {
    return new Observation(new GHPoint(latitude, longitude));
  }

  public GHPoint point() {
    return point;
  }

  public double lat() {
    return point.lat;
  }

  public double lon() {
    return point().lon;
  }


}

