package de.fleigm.ptmm.gtfs;

import lombok.Builder;
import lombok.Data;

import java.net.URL;

@Data
@Builder
public class Route {

  private String routeId;
  private String agencyId;
  private String shortName;
  private String longName;
  private String description;
  private int type;
  private URL url;
  private String color;
  private int sortOder;
  private String textColor;
  private URL brandingUrl;
  private String feedId;

}
