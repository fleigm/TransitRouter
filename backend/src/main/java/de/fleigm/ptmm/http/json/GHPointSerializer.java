package de.fleigm.ptmm.http.json;

import com.graphhopper.util.shapes.GHPoint;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class GHPointSerializer implements JsonbSerializer<GHPoint> {

  @Override
  public void serialize(GHPoint obj, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartArray()
        .write(obj.lat)
        .write(obj.lon)
        .writeEnd();
  }
}
