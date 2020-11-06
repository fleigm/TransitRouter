package de.fleigm.ptmm.http.json;

import com.graphhopper.matching.Observation;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class ObservationSerializer implements JsonbSerializer<Observation> {

  @Override
  public void serialize(Observation observation, JsonGenerator json, SerializationContext ctx) {
    json.writeStartArray()
        .write(observation.getPoint().lat)
        .write(observation.getPoint().lon)
        .writeEnd();
  }
}
