package de.fleigm.transitrouter.http.json;

import de.fleigm.transitrouter.http.views.View;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class ViewSerializer implements JsonbSerializer<View> {

  @Override
  public void serialize(View obj, JsonGenerator generator, SerializationContext ctx) {
    ctx.serialize(obj.bag(), generator);
  }
}
