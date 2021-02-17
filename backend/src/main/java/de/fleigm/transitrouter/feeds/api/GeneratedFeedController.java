package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.gtfs.FeedDetails;
import de.fleigm.transitrouter.presets.PresetRepository;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.json.Json;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("feeds")
public class GeneratedFeedController {

  @Inject
  FeedGenerationService feedGenerationService;

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  PresetRepository presetRepository;

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(@MultipartForm @Valid GenerateFeedRequest request) {
    FeedGenerationResponse feedGenerationResponse = feedGenerationService.create(request);

    return Response.status(Response.Status.CREATED)
        .entity(feedGenerationResponse.generatedFeed())
        .build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index() {
    List<GeneratedFeed> evaluations = generatedFeedRepository.all()
        .stream()
        .sorted(Comparator.comparing(GeneratedFeed::getCreatedAt).reversed())
        .collect(Collectors.toList());

    return Response.ok(evaluations).build();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response show(@PathParam("id") UUID id) {
    GeneratedFeed feed = generatedFeedRepository.findOrFail(id);

    if (feed.getPreset() != null && !feed.hasExtension(FeedDetails.class)) {
      presetRepository.findOrFail(feed.getPreset())
          .getExtension(FeedDetails.class)
          .ifPresent(feed::addExtension);
    }

    return Response.ok(feed).build();
  }

  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("id") UUID id) {
    GeneratedFeed feedInfo = generatedFeedRepository.findOrFail(id);


    if (feedInfo.isPending()) {
      return Response.status(Response.Status.CONFLICT)
          .entity(Json.createObjectBuilder()
              .add("message", "Can only delete finished or failed evaluations.")
              .build())
          .build();
    }

    generatedFeedRepository.delete(id);

    return Response.noContent().build();

  }
}