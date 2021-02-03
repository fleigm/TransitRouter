package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
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

@Path("eval")
public class EvaluationController {

  @Inject
  EvaluationService evaluationService;

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(@MultipartForm @Valid CreateEvaluationRequest request) {
    EvaluationResponse evaluationResponse = evaluationService.createEvaluation(request);

    return Response.status(Response.Status.CREATED)
        .entity(evaluationResponse.info())
        .build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index() {
    List<GeneratedFeedInfo> evaluations = generatedFeedRepository.all()
        .stream()
        .sorted(Comparator.comparing(GeneratedFeedInfo::getCreatedAt).reversed())
        .collect(Collectors.toList());

    return Response.ok(evaluations).build();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response show(@PathParam("id") UUID id) {
    return generatedFeedRepository.find(id)
        .map(Response::ok)
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();
  }

  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("id") UUID id) {
    GeneratedFeedInfo feedInfo = generatedFeedRepository.findOrFail(id);


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
