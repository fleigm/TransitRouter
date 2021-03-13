package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.graphhopper.routing.Path;

import java.io.IOException;

public class PathSerializer extends StdSerializer<Path> {

  public PathSerializer() {
    super(Path.class);
  }

  @Override
  public void serialize(Path value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    provider.defaultSerializeValue(value.calcPoints(), gen);
  }
}
