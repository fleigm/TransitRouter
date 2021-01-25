package de.fleigm.ptmm.http.json;

import de.fleigm.ptmm.data.Extensions;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;

public class ExtensionsDeserializer implements JsonbDeserializer<Extensions> {

  @Override
  public Extensions deserialize(JsonParser parser, DeserializationContext context, Type rtType) {
    Extensions extensions = new Extensions();

    while (parser.hasNext()) {
      JsonParser.Event event = parser.next();
      if (event == JsonParser.Event.KEY_NAME) {
        String className = parser.getString();
        parser.next();
        try {
          Object extension = context.deserialize(Class.forName(className), parser);
          extensions.add(extension);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return extensions;
  }
}
