package de.fleigm.transitrouter.feeds.process;

import de.fleigm.transitrouter.data.Extension;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionTime implements Extension {

  private Map<String, Duration> durations = new HashMap<>();

  public ExecutionTime add(String key, Duration duration) {
    durations.put(key, duration);
    return this;
  }
}
