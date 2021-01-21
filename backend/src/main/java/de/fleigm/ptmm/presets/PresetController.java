package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.eval.process.ValidateGtfsFeed;
import de.fleigm.ptmm.util.Unzip;
import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Path("presets")
@Produces(MediaType.APPLICATION_JSON)
public class PresetController {

  @Inject
  PresetRepository presets;

  @GET
  public Response index() {
    return Response.ok(presets.all()).build();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(
      @NotNull @Valid @MultipartForm PresetUploadForm presetUploadForm,
      @Context UriInfo uriInfo)
  {
    Preset preset = Preset.builder()
        .name(presetUploadForm.getName())
        .createdAt(LocalDateTime.now())
        .build();

    var path = presets.entityStoragePath(preset);

    preset.setPath(path);

    try {
      File file = path.resolve("gtfs.zip").toFile();
      FileUtils.copyInputStreamToFile(presetUploadForm.getGtfsFeed(), file);

      if (!ValidateGtfsFeed.validate(file.toPath())) {
        FileUtils.deleteDirectory(path.toFile());
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      Unzip.apply(path.resolve("gtfs.zip"), path.resolve("gtfs"));

      presets.save(preset);

    } catch (IOException e) {
      return Response.serverError().build();
    }

    return Response.created(uriInfo.getAbsolutePathBuilder().build(preset.getId()))
        .entity(preset)
        .build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") UUID id) {
    return presets.find(id)
        .map(Response::ok)
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") UUID id) {
    presets.find(id).ifPresent(presets::delete);

    return Response.status(Response.Status.NO_CONTENT).build();
  }

}
