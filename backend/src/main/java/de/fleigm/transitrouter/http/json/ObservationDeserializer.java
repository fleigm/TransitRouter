package de.fleigm.transitrouter.http.json;

import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.transitrouter.routing.Observation;

import javax.json.JsonArray;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;

public class ObservationDeserializer implements JsonbDeserializer<Observation> {

  @Override
  public Observation deserialize(JsonParser jsonParser, DeserializationContext deserializationContext, Type type) {
    JsonArray array = jsonParser.getArray();

    GHPoint point = new GHPoint(
        array.getJsonNumber(0).doubleValue(),
        array.getJsonNumber(1).doubleValue());

    return new Observation(point);
  }
}
