package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.eval.Parameters;
import de.fleigm.ptmm.eval.api.EvaluationResponse;
import de.fleigm.ptmm.eval.api.EvaluationService;
import de.fleigm.ptmm.events.CreatedQualifier;
import de.fleigm.ptmm.events.Events;
import de.fleigm.ptmm.feeds.Feed;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("presets")
@Produces(MediaType.APPLICATION_JSON)
public class PresetController {

  @ConfigProperty(name = "app.storage")
  java.nio.file.Path storagePath;

  @Inject
  Events events;

  @Inject
  PresetRepository presets;

  @Inject
  EvaluationService evaluationService;

  @GET
  public Response index() {
    return Response.ok(
        presets.all()
            .stream()
            .sorted(Comparator.comparing(Preset::getCreatedAt).reversed())
            .collect(Collectors.toList())
    ).build();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(
      @NotNull @Valid @MultipartForm PresetUploadForm presetUploadForm,
      @Context UriInfo uriInfo) {

    Preset preset = Preset.builder()
        .name(presetUploadForm.getName())
        .build();

    preset.setFeed(Feed.create(preset.getFileStoragePath().resolve("gtfs.zip"), presetUploadForm.getGtfsFeed()));

    presets.save(preset);

    events.fire(preset, new CreatedQualifier());

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

  @POST
  @Path("{id}/generated-feeds")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response generateFeed(@PathParam("id") UUID id, @NotNull @Valid GenerateFeedRequest generateFeedRequest) {

    Optional<Preset> preset = presets.find(id);

    if (preset.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    EvaluationResponse evaluationResponse = evaluationService.createFromPreset(
        preset.get(),
        generateFeedRequest.getName(),
        Parameters.builder()
            .sigma(generateFeedRequest.getSigma())
            .candidateSearchRadius(generateFeedRequest.getCandidateSearchRadius())
            .beta(generateFeedRequest.getBeta())
            .profile(generateFeedRequest.getProfile())
            .build());

    return Response.ok(evaluationResponse.info()).build();

  }

  @GET
  @Path("{id}/generated-feeds")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getGeneratedFeeds(@PathParam("id") UUID id) {
    if (presets.find(id).isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response.ok(presets.generatedFeedsFromPreset(id)).build();
  }

}
