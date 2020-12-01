package de.fleigm.ptmm.http;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;
import java.util.UUID;

@Provider
@ApplicationScoped
public class UUIDParamConverter implements ParamConverter<UUID> {

  @Override
  public UUID fromString(String value) {
    return UUID.fromString(value);
  }

  @Override
  public String toString(UUID value) {
    return value.toString();
  }
}
