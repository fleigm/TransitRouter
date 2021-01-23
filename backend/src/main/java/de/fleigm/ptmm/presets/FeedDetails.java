package de.fleigm.ptmm.presets;

import com.conveyal.gtfs.model.Agency;
import com.conveyal.gtfs.model.FeedInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedDetails {
  private FeedInfo info;
  private List<Agency> agencies;
  private int routes;
  private int trips;
  private Map<Integer, Long> routesPerType;
  private Map<Integer, Long> tripsPerType;
}
