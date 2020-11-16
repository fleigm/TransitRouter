package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.FileEvaluationRepository;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("commands/eval")
public class EvaluationCommandsController {

  @Inject
  FileEvaluationRepository evaluationRepository;

  @POST
  @Path("clear-cache")
  public Response clearInfoCache() {
    evaluationRepository.invalidateCache();

    return Response.ok().build();
  }

}
