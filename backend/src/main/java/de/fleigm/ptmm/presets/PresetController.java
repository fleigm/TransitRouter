package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.App;
import de.fleigm.ptmm.events.CreatedQualifier;
import de.fleigm.ptmm.events.Events;
import de.fleigm.ptmm.util.Unzip;
import de.fleigm.ptmm.util.ValidateGtfsFeed;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("presets")
@Produces(MediaType.APPLICATION_JSON)
public class PresetController {

  @ConfigProperty(name = "app.storage")
  java.nio.file.Path storagePath;

  @Inject
  PresetRepository presets;

  @Inject
  Events events;

  @Inject
  App app;

  @GET
  public Response index() {
    List<Preset> collect = app.data().presets().all()
        .stream()
        .sorted(Comparator.comparing(Preset::getCreatedAt).reversed())
        .collect(Collectors.toList());
    return Response.ok(
        collect
    ).build();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(
      @NotNull @Valid @MultipartForm PresetUploadForm presetUploadForm,
      @Context UriInfo uriInfo) {
    Preset preset = Preset.builder()
        .name(presetUploadForm.getName())
        .createdAt(LocalDateTime.now())
        .build();

   preset.setStoragePath(storagePath.resolve("presets"));

    try {
      File file = preset.getPath().resolve("gtfs.zip").toFile();
      FileUtils.copyInputStreamToFile(presetUploadForm.getGtfsFeed(), file);

      if (!ValidateGtfsFeed.validate(file.toPath())) {
        FileUtils.deleteDirectory(preset.getPath().toFile());
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      Unzip.apply(preset.getPath().resolve("gtfs.zip"), preset.getPath().resolve("gtfs"));

      app.data().presets().save(preset);

    } catch (IOException e) {
      return Response.serverError().build();
    }

    events.fire(preset, new CreatedQualifier());

    return Response.created(uriInfo.getAbsolutePathBuilder().build(preset.getId()))
        .entity(preset)
        .build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") UUID id) {
    return app.data().presets().find(id)
        .map(Response::ok)
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") UUID id) {
    app.data().presets().find(id).ifPresent(app.data().presets()::delete);

    return Response.status(Response.Status.NO_CONTENT).build();
  }

}
