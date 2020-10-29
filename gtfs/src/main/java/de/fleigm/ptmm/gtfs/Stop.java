package de.fleigm.ptmm.gtfs;

import lombok.Builder;
import lombok.Data;

import java.net.URL;

@Data
@Builder
public class Stop {
  private String id;
  private String code;
  private String name;
  private String description;
  private double latitude;
  private double longitude;
  private String zoneId;
  private URL url;
  private int locationType;
  private String parentStation;
  private String timezone;
  private String wheelchairBoarding;
  private String feedId;
}
