package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.graphhopper.util.EdgeIteratorState;

import java.io.IOException;

public class EdgeIteratorStateSerializer extends StdSerializer<EdgeIteratorState> {

  public EdgeIteratorStateSerializer() {
    super(EdgeIteratorState.class);
  }

  @Override
  public void serialize(EdgeIteratorState value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeNumber(value.getEdge());
  }
}
