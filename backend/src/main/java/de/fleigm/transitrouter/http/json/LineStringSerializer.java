package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import java.io.IOException;

public class LineStringSerializer extends StdSerializer<LineString> {

  public LineStringSerializer() {
    super(LineString.class);
  }

  @Override
  public void serialize(LineString value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartArray();

    for (int i = 0; i < value.getNumPoints(); i++) {
      Point point = value.getPointN(i);

      gen.writeStartArray();
      gen.writeNumber(point.getY());
      gen.writeNumber(point.getX());
      gen.writeEndArray();
    }

    gen.writeEndArray();
  }
}
