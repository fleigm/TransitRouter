package de.fleigm.ptmm.feeds.evaluation;

import de.fleigm.ptmm.feeds.GeneratedFeed;
import de.fleigm.ptmm.feeds.GeneratedFeedRepository;
import de.fleigm.ptmm.feeds.Status;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

@Path("feeds/{id}/report")
public class EvaluationReportController {
  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  ReportService reportService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index(@PathParam("id") UUID id) {
    Optional<GeneratedFeed> info = generatedFeedRepository.find(id);

    if (info.isEmpty() || info.get().getStatus() != Status.FINISHED) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return info.flatMap(i -> i.getExtension(Evaluation.class))
        .map(evaluation -> reportService.get(evaluation.getReport()))
        .map(Response::ok)
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();

    /*return generatedFeedRepository.findEvaluationResult(id)
        .map(evaluationResult -> getPagedReport(evaluationResult, uriInfo, paged, search, sort))
        .orElse(Response.status(Response.Status.NOT_FOUND).build());*/
  }

}
