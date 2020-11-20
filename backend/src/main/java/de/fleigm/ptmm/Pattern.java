package de.fleigm.ptmm;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class Pattern {
  private final Route route;
  private final List<Stop> stops;
  private final List<Trip> trips;
}
