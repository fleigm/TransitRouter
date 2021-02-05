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
  default <T extends Extension> Optional<T> getExtension(Class<T> extension) {
    return extensions().get(extension);
  }

  /**
   * @see Extensions#getOrCreate(Class, Supplier)
   */
  default <T extends Extension> T getOrCreateExtension(Class<T> extension, Supplier<T> defaultSupplier) {
    return extensions().getOrCreate(extension, defaultSupplier);
  }

  /**
   * @see Extensions#has(Class)
   */
  default <T extends Extension> boolean hasExtension(Class<T> extension) {
    return extensions().has(extension);
  }

  /**
   * @see Extensions#add(Extension) 
   */
  default <T extends Extension> HasExtensions addExtension(T extension) {
    extensions().add(extension);
    return this;
  }
}
