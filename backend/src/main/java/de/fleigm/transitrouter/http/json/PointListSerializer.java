package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.graphhopper.util.PointList;

import java.io.IOException;

public class PointListSerializer extends StdSerializer<PointList> {

  public PointListSerializer() {
    super(PointList.class);
  }

  @Override
  public void serialize(PointList value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartArray();

    for (int i = 0; i < value.size(); i++) {
      gen.writeStartArray();
      gen.writeNumber(value.getLatitude(i));
      gen.writeNumber(value.getLongitude(i));
      gen.writeEndArray();
    }

    gen.writeEndArray();
  }
}
