package de.fleigm.transitrouter.http.json;

import com.conveyal.gtfs.model.Entity;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.transitrouter.routing.Observation;
import io.quarkus.jackson.ObjectMapperCustomizer;

import javax.inject.Singleton;

@Singleton
public class JsonbConfiguration implements ObjectMapperCustomizer {

  @Override
  public void customize(ObjectMapper objectMapper) {
    objectMapper.registerModule(new SimpleModule()
        .addSerializer(new PointListSerializer())
        .addSerializer(new LineStringSerializer())
        .addSerializer(new PathSerializer())
        .addSerializer(new EdgeIteratorStateSerializer())
        .addSerializer(new GHPointSerializer())
        .addSerializer(new ObservationSerializer())
        .addDeserializer(GHPoint.class, new GHPointDeserializer())
        .addDeserializer(Observation.class, new ObservationDeserializer()));


    objectMapper
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addMixIn(Entity.class, ConveyalIgnoreIdMixin.class)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
  }
}
