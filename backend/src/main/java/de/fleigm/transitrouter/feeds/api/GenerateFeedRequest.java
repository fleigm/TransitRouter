package de.fleigm.transitrouter.feeds.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Data
@Builder
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

  @FormParam("profile")
  @NotBlank
  private String profile;

  @FormParam("sigma")
  @NotNull
  @Positive
  private Double sigma;

  @FormParam("candidateSearchRadius")
  @NotNull
  @Positive
  private Double candidateSearchRadius;

  @FormParam("beta")
  @NotNull
  @Positive
  private Double beta;

  @FormParam("useGraphHopperMapMatching")
  private boolean useGraphHopperMapMatching;

  @FormParam("withEvaluation")
  private boolean withEvaluation = true;

}
