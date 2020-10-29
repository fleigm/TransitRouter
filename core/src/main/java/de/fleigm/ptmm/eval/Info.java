package de.fleigm.ptmm.eval;

import java.time.LocalDateTime;
import java.util.Map;

public class Info {

  public static final String STATUS = "status";
  public static final String NAME = "name";

  private Map<String, Object> map;

  public String name() {
    return getAs("name", String.class);
  }

  public String status() {
    return getAs("status", String.class);
  }

  public LocalDateTime createdAt() {
    return getAs("createdAt", LocalDateTime.class);
  }

  public Integer processedShapes() {
    return getAs("statistics.trips", Integer.class);
  }

  public Integer generatedShapes() {
    return getAs("statistics.generated_shapes", Integer.class);
  }


  private <T> T getAs(String key, Class<T> type) {
    Object value = map.get(key);

    if (value == null) {
      return null;
    }

    if (!type.isAssignableFrom(value.getClass())) {
      throw new IllegalArgumentException(
          String.format("Tried to get %s of type %s as type %s", key, value.getClass(), type));
    }

    return (T) value;
  }
}
