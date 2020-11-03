package de.fleigm.ptmm.http.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

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
  private InputStream gtfsFeed;

  @FormParam("name")
  @PartType(MediaType.TEXT_PLAIN)
  private String name;

}
