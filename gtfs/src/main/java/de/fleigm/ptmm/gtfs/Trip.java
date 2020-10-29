package de.fleigm.ptmm.gtfs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Trip {
  private String routeId;
  private String serviceId;
  private String id;
  private String headsign;
  private String shortName;
  private int directionId;
  private String blockId;
  private String shapeId;
  private int bikesAllowed;
  private int wheelchairAccessible;
  private String feedId;
}
