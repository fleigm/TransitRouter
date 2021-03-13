package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fleigm.transitrouter.routing.Observation;

import java.io.IOException;

public class ObservationSerializer extends StdSerializer<Observation> {

  public ObservationSerializer() {
    super(Observation.class);
  }

  @Override
  public void serialize(Observation value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartArray();
    gen.writeNumber(value.lat());
    gen.writeNumber(value.lon());
    gen.writeEndArray();
  }
}
