package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.gtfs.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateFeedRequest {

  @FormParam("feed")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  @NotNull
  private InputStream gtfsFeed;

  @FormParam("name")
  @PartType(MediaType.TEXT_PLAIN)
  @NotBlank
  private String name;

  @FormParam("parameters")
  @NotBlank
  private String parameters;

  @FormParam("withEvaluation")
  private boolean withEvaluation = true;

  public static GenerateFeedRequestBuilder builder() {
    return new GenerateFeedRequestBuilder();
  }

  public Map<Type, Parameters> getParameters() {
    Jsonb jsonb = JsonbBuilder.create();
    Map<String, Parameters> params = jsonb.fromJson(parameters, new HashMap<String, Parameters>(){}.getClass().getGenericSuperclass());

    return params.entrySet().stream()
        .map(entry -> Map.entry(Type.valueOf(entry.getKey()),  entry.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }


  public static class GenerateFeedRequestBuilder {
    private InputStream gtfsFeed;
    private String name;
    private Map<Type, Parameters> parameters = new HashMap<>();
    private boolean withEvaluation;

    GenerateFeedRequestBuilder() {
    }

    public GenerateFeedRequestBuilder gtfsFeed(@NotNull InputStream gtfsFeed) {
      this.gtfsFeed = gtfsFeed;
      return this;
    }

    public GenerateFeedRequestBuilder name(@NotBlank String name) {
      this.name = name;
      return this;
    }

    public GenerateFeedRequestBuilder parameters(Type type, Parameters parameters) {
      this.parameters.put(type, parameters);
      return this;
    }

    public GenerateFeedRequestBuilder withEvaluation(boolean withEvaluation) {
      this.withEvaluation = withEvaluation;
      return this;
    }

    public GenerateFeedRequest build() {
      Jsonb jsonb = JsonbBuilder.create();

      return new GenerateFeedRequest(gtfsFeed, name, jsonb.toJson(parameters), withEvaluation);
    }
  }
}
