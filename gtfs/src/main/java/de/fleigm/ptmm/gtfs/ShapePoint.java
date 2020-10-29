package de.fleigm.ptmm.gtfs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShapePoint {
  private String id;
  private double latitude;
  private double longitude;
  private int sequence;
  private double shapeDistTraveled;
}
