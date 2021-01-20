package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("eval")
public class EvaluationController {

  @Inject
  EvaluationService evaluationService;

  @Inject
  EvaluationRepository evaluationRepository;

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
    List<GeneratedFeedInfo> evaluations = evaluationRepository.all()
        .stream()
        .sorted(Comparator.comparing(GeneratedFeedInfo::getCreatedAt).reversed())
        .collect(Collectors.toList());

    return Response.ok(evaluations).build();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response show(@PathParam("id") UUID id) {
    return evaluationRepository.find(id)
        .map(info -> Response.ok(info).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("id") UUID id) {
    Optional<Exception> result = evaluationRepository.delete(id);

    if (result.isEmpty()) {
      return Response.noContent().build();
    }

    if (result.get().getClass().equals(IllegalStateException.class)) {
      return Response.status(Response.Status.CONFLICT)
          .entity(Json.createObjectBuilder()
              .add("message", "Can only delete finished or failed evaluations.")
              .build())
          .build();
    }

    return Response.serverError().build();
  }
}
