package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.graphhopper.util.shapes.GHPoint;

import java.io.IOException;

public class GHPointSerializer extends StdSerializer<GHPoint> {

  public GHPointSerializer() {
    super(GHPoint.class);
  }

  @Override
  public void serialize(GHPoint value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
      gen.writeStartArray();
      gen.writeNumber(value.lat);
      gen.writeNumber(value.lon);
      gen.writeEndArray();
  }
}
