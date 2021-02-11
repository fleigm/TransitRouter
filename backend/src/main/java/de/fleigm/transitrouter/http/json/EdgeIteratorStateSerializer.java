package de.fleigm.transitrouter.http.json;

import com.graphhopper.util.EdgeIteratorState;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class EdgeIteratorStateSerializer implements JsonbSerializer<EdgeIteratorState> {

  @Override
  public void serialize(EdgeIteratorState obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write(obj.getEdge());
  }
}
