package de.fleigm.ptmm;

import com.graphhopper.util.PointList;

public class PointListBuilder {
  private PointList pointList = new PointList();


  public PointListBuilder add(double lat, double lon) {
    pointList.add(lat, lon);
    return this;
  }

  public PointList create() {
    return pointList;
  }


}
