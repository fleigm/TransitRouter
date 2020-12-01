package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.EvaluationRepository;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("commands/eval")
public class EvaluationCommandsController {

  @Inject
  EvaluationRepository evaluationRepository;

  @POST
  @Path("clear-cache")
  public Response clearInfoCache() {
    evaluationRepository.invalidateCache();

    return Response.ok().build();
  }

}
