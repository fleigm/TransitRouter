package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.GeneratedFeedRepository;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("commands/eval")
public class EvaluationCommandsController {

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @POST
  @Path("clear-cache")
  public Response clearInfoCache() {
    generatedFeedRepository.invalidateCache();

    return Response.ok().build();
  }

}
