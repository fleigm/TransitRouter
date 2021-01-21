package de.fleigm.ptmm.data;

import java.util.Optional;
import java.util.function.Supplier;

public interface HasExtensions {

  Extensions extensions();

  default <T> Optional<T> getExtension(Class<T> extension) {
    return extensions().get(extension);
  }

  default <T> T getOrCreateExtension(Class<T> extension, Supplier<T> defaultSupplier) {
    return extensions().getOrCreate(extension, defaultSupplier);
  }

  default <T> boolean hasExtension(Class<T> extension) {
    return extensions().has(extension);
  }

  default <T> HasExtensions addExtension(T extension) {
    addExtension(extension);
    return this;
  }
}
