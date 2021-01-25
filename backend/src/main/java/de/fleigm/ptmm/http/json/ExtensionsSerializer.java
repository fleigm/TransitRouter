package de.fleigm.ptmm.http.json;

import de.fleigm.ptmm.data.Extensions;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import java.util.Map;

public class ExtensionsSerializer implements JsonbSerializer<Extensions> {

  @Override
  public void serialize(Extensions extensions, JsonGenerator generator, SerializationContext context) {
    Map<Class<?>, Object> rawExtensions = extensions.unwrap();
    /*rawExtensions.entrySet().stream().findFirst()
        .map(x -> context.serialize(x.getKey()))*/
    generator.writeStartObject();
    rawExtensions.forEach((type, ext) -> {
      context.serialize(type.getName(), ext, generator);

    });
    generator.writeEnd();

  }
}
