package de.fleigm.ptmm.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class Extensions {

  private final Map<Class<?>, Object> extensions = new HashMap<>();

  public <T> Optional<T> get(Class<T> extension) {
    return (Optional<T>) Optional.ofNullable(extensions.get(extension));
  }

  public <T> T getOrCreate(Class<T> extension, Supplier<T> defaultSupplier) {
    return (T) extensions.computeIfAbsent(extension, x -> defaultSupplier.get());
  }

  public <T> boolean has(Class<T> extension) {
    return extensions.containsKey(extension);
  }

  public <T> Extensions add(T extension) {
    extensions.put(extension.getClass(), extension);
    return this;
  }
}
