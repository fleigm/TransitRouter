package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.EvaluationResult;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.http.pagination.Page;
import de.fleigm.ptmm.http.pagination.Paged;
import de.fleigm.ptmm.http.sort.SortQuery;
import de.fleigm.ptmm.http.views.View;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("eval/{id}/report")
public class EvaluationReportController {
  @Inject
  EvaluationRepository evaluationRepository;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index(
      @PathParam("id") UUID id,
      @Context UriInfo uriInfo,
      @BeanParam Paged paged,
      @QueryParam("search") @DefaultValue("") String search,
      @QueryParam("sort") @DefaultValue("") String sort) {

    Optional<GeneratedFeedInfo> info = evaluationRepository.find(id);

    if (info.isEmpty() || info.get().getStatus() != Status.FINISHED) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return evaluationRepository.findEvaluationResult(id)
        .map(evaluationResult -> getPagedReport(evaluationResult, uriInfo, paged, search, sort))
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  private Response getPagedReport(EvaluationResult evaluationResult,
                                  UriInfo uriInfo,
                                  Paged paged,
                                  String search,
                                  String sort) {

    Report report = evaluationResult.report();
    Stream<Report.Entry> entryQueryStream = report.entries().stream();

    if (!search.isBlank()) {
      entryQueryStream = entryQueryStream.filter(entry -> entry.tripId.contains(search));
    }

    if (!sort.isBlank()) {
      Comparator<Report.Entry> comparator = createComparator(SortQuery.parse(sort));
      if (comparator != null) {
        if (SortQuery.parse(sort).order() == SortQuery.SortOrder.DESC) {
          comparator = comparator.reversed();
        }
        entryQueryStream = entryQueryStream.sorted(comparator);
      }
    }

    TransitFeed transitFeed = evaluationResult.generatedTransitFeed();

    List<Report.Entry> entries = entryQueryStream.collect(Collectors.toList());

    List<View> views = entries.stream()
        .skip(paged.getOffset())
        .limit(paged.getLimit())
        .map(entry -> new View()
            .add("tripId", entry.tripId())
            .add("an", entry.an())
            .add("al", entry.al())
            .add("avgFd", entry.avgFd())
            .add("route", transitFeed.getRouteForTrip(entry.tripId()).route_short_name))
        .collect(Collectors.toList());

    var page = Page.<View>builder()
        .data(views)
        .currentPage(paged.getPage())
        .perPage(paged.getLimit())
        .total(entries.size())
        .uri(uriInfo.getAbsolutePath())
        .build();

    return Response.ok(page).build();
  }

  private Comparator<Report.Entry> createComparator(SortQuery sortQuery) {
    Comparator<Report.Entry> comparator = null;
    switch (sortQuery.attribute()) {
      case "an":
        comparator = Comparator.comparingDouble(Report.Entry::an);
        break;
      case "al":
        comparator = Comparator.comparingDouble(Report.Entry::al);
        break;
      case "avgFd":
        comparator = Comparator.comparingDouble(Report.Entry::avgFd);
        break;
    }
    return comparator;
  }

}
