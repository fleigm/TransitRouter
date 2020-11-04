package de.fleigm.ptmm.http.eval;

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
public class CreateEvaluationRequest {

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

  @FormParam("alpha")
  @NotNull
  @Positive
  private Double alpha;

  @FormParam("candidateSearchRadius")
  @NotNull
  @Positive
  private Double candidateSearchRadius;

  @FormParam("beta")
  @NotNull
  @Positive
  private Double beta;

  @FormParam("uTurnDistancePenalty")
  @NotNull
  @Positive
  private Double uTurnDistancePenalty;

}
