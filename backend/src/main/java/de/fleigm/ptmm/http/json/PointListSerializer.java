package de.fleigm.ptmm.http.json;

import com.graphhopper.util.PointList;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class PointListSerializer implements JsonbSerializer<PointList> {

  @Override
  public void serialize(PointList points, JsonGenerator json, SerializationContext serializationContext) {
    json.writeStartArray();

    for (int i = 0; i < points.size(); i++) {
      json.writeStartArray();
      json.write(points.getLatitude(i));
      json.write(points.getLongitude(i));
      json.writeEnd();
    }

    json.writeEnd();
  }
}
