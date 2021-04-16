package de.fleigm.transitrouter.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base interface for all extension.
 * Is required for (de)-serialization
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface Extension {
}
