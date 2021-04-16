package de.fleigm.transitrouter.presets;

import de.fleigm.transitrouter.events.CreatedQualifier;
import de.fleigm.transitrouter.events.Events;
import de.fleigm.transitrouter.feeds.api.FeedGenerationResponse;
import de.fleigm.transitrouter.feeds.api.FeedGenerationService;
import de.fleigm.transitrouter.gtfs.Feed;
import de.fleigm.transitrouter.util.Helper;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("presets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PresetController {
  @Inject
  Events events;

  @Inject
  PresetRepository presets;

  @Inject
  FeedGenerationService feedGenerationService;

  @GET
  public List<Preset> index() {
    return presets.all()
        .stream()
        .sorted(Comparator.comparing(Preset::getCreatedAt).reversed())
        .collect(Collectors.toList());
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response create(
      @NotNull @Valid @MultipartForm PresetUploadForm presetUploadForm,
      @Context UriInfo uriInfo) {

    Preset preset = Preset.builder()
        .name(presetUploadForm.getName())
        .build();

    Feed feed = Feed.create(
        preset.getFileStoragePath().resolve("gtfs.zip"),
        presetUploadForm.getGtfsFeed());

    preset.setFeed(feed);

    presets.save(preset);

    events.fire(preset, new CreatedQualifier());

    return Response.created(uriInfo.getAbsolutePathBuilder().build(preset.getId()))
        .entity(preset)
        .build();
  }

  @GET
  @Path("{id}")
  public Preset get(@PathParam("id") UUID id) {
    return presets.findOrFail(id);
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") UUID id) {
    presets.find(id).ifPresent(presets::delete);

    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @POST
  @Path("{id}/generated-feeds")
  public Response generateFeed(@PathParam("id") UUID id, GenerateFeedRequest generateFeedRequest) {
    Preset preset = presets.findOrFail(id);

    FeedGenerationResponse feedGenerationResponse = feedGenerationService.createFromPreset(
        preset,
        generateFeedRequest.getName(),
        generateFeedRequest.getParameters(),
        generateFeedRequest.isWithEvaluation());

    return Response.ok(feedGenerationResponse.generatedFeed()).build();

  }

  @GET
  @Path("{id}/generated-feeds")
  public Response getGeneratedFeeds(@PathParam("id") UUID id) {
    return presets.find(id)
        .map(presets::generatedFeedsFromPreset)
        .map(Response::ok)
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();
  }

  @GET
  @Path("{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response download(@PathParam("id") UUID id) throws IOException {
    Preset preset = presets.findOrFail(id);

    List<java.nio.file.Path> files = Files.list(preset.getFileStoragePath())
        .filter(path -> !Files.isDirectory(path))
        .collect(Collectors.toList());

    return Response
        .ok((StreamingOutput) output -> Helper.buildZipFile(output, files))
        .header("Content-Disposition", "attachment;filename=" + preset.getName() + ".zip")
        .build();
  }


}
