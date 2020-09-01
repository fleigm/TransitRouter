package de.fleigm.ptmm.http.json;

import de.fleigm.ptmm.http.json.PointListSerializer;
import io.quarkus.jsonb.JsonbConfigCustomizer;

import javax.inject.Singleton;
import javax.json.bind.JsonbConfig;

@Singleton
public class JsonbConfiguration implements JsonbConfigCustomizer {

  @Override
  public void customize(JsonbConfig config) {
    config.withSerializers(new PointListSerializer())
        .withSerializers(new GHPointSerializer())
        .withSerializers(new ObservationSerializer())
        .withSerializers(new StateSerializer());
  }
}
