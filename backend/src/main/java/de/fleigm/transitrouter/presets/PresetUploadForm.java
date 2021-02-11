package de.fleigm.transitrouter.presets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PresetUploadForm {

  @FormParam("feed")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  @NotNull
  private InputStream gtfsFeed;

  @FormParam("name")
  @PartType(MediaType.TEXT_PLAIN)
  @NotBlank
  private String name;
}
