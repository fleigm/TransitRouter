package de.fleigm.transitrouter.http.json;

import com.graphhopper.routing.Path;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class PathSerializer implements JsonbSerializer<Path> {

  @Override
  public void serialize(Path path, JsonGenerator generator, SerializationContext ctx) {
    ctx.serialize(path.calcPoints(), generator);
  }
}
