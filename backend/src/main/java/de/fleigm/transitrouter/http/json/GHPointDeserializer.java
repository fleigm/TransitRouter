package de.fleigm.transitrouter.http.json;

import com.graphhopper.util.shapes.GHPoint;

import javax.json.JsonArray;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;

public class GHPointDeserializer implements JsonbDeserializer<GHPoint> {

  @Override
  public GHPoint deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
    JsonArray array = jsonParser.getArray();
    
    return new GHPoint(
        array.getJsonNumber(0).doubleValue(),
        array.getJsonNumber(1).doubleValue()
    );
  }
}
