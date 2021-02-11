package de.fleigm.transitrouter.http.json;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class LineStringSerializer implements JsonbSerializer<LineString> {

  @Override
  public void serialize(LineString obj, JsonGenerator json, SerializationContext serializationContext) {
    json.writeStartArray();

    for (int i = 0; i < obj.getNumPoints(); i++) {
      Point point = obj.getPointN(i);

      json.writeStartArray();
      json.write(point.getY());
      json.write(point.getX());
      json.writeEnd();
    }

    json.writeEnd();
  }
}
