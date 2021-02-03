package de.fleigm.ptmm.data;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provide convenient methods for entities containing extensions,
 */
public interface HasExtensions {

  /**
   * @return get extensions
   */
  Extensions extensions();

  /**
   * @see Extensions#get(Class)
   */
  default <T> Optional<T> getExtension(Class<T> extension) {
    return extensions().get(extension);
  }

  /**
   * @see Extensions#getOrCreate(Class, Supplier)
   */
  default <T> T getOrCreateExtension(Class<T> extension, Supplier<T> defaultSupplier) {
    return extensions().getOrCreate(extension, defaultSupplier);
  }

  /**
   * @see Extensions#has(Class)
   */
  default <T> boolean hasExtension(Class<T> extension) {
    return extensions().has(extension);
  }

  /**
   * @see Extensions#add(Object)
   */
  default <T> HasExtensions addExtension(T extension) {
    extensions().add(extension);
    return this;
  }
}
