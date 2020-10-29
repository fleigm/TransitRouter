package de.fleigm.ptmm.gtfs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StopTime {
  private String tripId;
  private int arrivalTime;
  private int departureTime;
  private String stopId;
  private int sequence;
  private String headsign;
  private int pickupType;
  private int dropOffType;
  private double shapeDistTraveled;
  private int timepoint = Integer.MIN_VALUE;

}
