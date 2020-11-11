package de.fleigm.ptmm.eval.api;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.vividsolutions.jts.geom.LineString;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.EvaluationResult;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.ReportEntry;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.http.pagination.Page;
import de.fleigm.ptmm.http.pagination.Paged;
import de.fleigm.ptmm.http.sort.SortQuery;
import de.fleigm.ptmm.http.views.View;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    evaluationService.createEvaluation(request);

    return Response.ok().build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index() {
    List<Info> evaluations = evaluationRepository.all()
        .stream()
        .sorted(Comparator.comparing(Info::getCreatedAt).reversed())
        .collect(Collectors.toList());

    return Response.ok(evaluations).build();
  }

  @GET
  @Path("{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@PathParam("name") String name) {
    return evaluationRepository.find(name)
        .map(info -> Response.ok(info).build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @GET
  @Path("{name}/trips")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTrips(
      @PathParam("name") String name,
      @Context UriInfo uriInfo,
      @BeanParam Paged paged,
      @QueryParam("search") @DefaultValue("") String search,
      @QueryParam("sort") @DefaultValue("") String sort) {

    Optional<Info> info = evaluationRepository.find(name);

    if (info.isEmpty() || info.get().getStatus() != Status.FINISHED) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Optional<EvaluationResult> evaluationResult = evaluationRepository.findEvaluationResult(name);

    if (evaluationResult.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Report report = evaluationResult.get().report();

    Stream<ReportEntry> entries = report.entries().stream();

    if (!search.isBlank()) {
      entries = entries.filter(reportEntry -> reportEntry.tripId.contains(search));
    }

    if (!sort.isBlank()) {
      Comparator<ReportEntry> comparator = createComparator(SortQuery.parse(sort));
      if (comparator != null) {
        if (SortQuery.parse(sort).order() == SortQuery.SortOrder.DESC) {
          comparator = comparator.reversed();
        }
        entries = entries.sorted(comparator);
      }
    }

    TransitFeed transitFeed = evaluationResult.get().generatedTransitFeed();

    List<View> views = entries
        .skip(paged.getOffset())
        .limit(paged.getLimit())
        .map(entry -> new View()
            .add("tripId", entry.tripId())
            .add("an", entry.an())
            .add("al", entry.al())
            .add("avgFd", entry.avgFd())
            .add("route", transitFeed.getRouteForTrip(entry.tripId()).route_short_name))
        .collect(Collectors.toList());

    var page = Page.builder()
        .setData(views)
        .setCurrentPage(paged.getPage())
        .setPerPage(paged.getLimit())
        .setTotal(views.size())
        .setUri(uriInfo.getAbsolutePath())
        .create();

    return Response.ok(page).build();
  }

  @GET
  @Path("{name}/trips/{tripId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDetails(@PathParam("name") String name, @PathParam("tripId") String tripId) {
    Optional<EvaluationResult> evaluationResult = evaluationRepository.findEvaluationResult(name);

    if (evaluationResult.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    TransitFeed originalFeed = evaluationResult.get().originalTransitFeed();
    TransitFeed generatedFeed = evaluationResult.get().generatedTransitFeed();

    Trip trip = generatedFeed.internal().trips.get(tripId);
    Route route = generatedFeed.getRouteForTrip(tripId);
    List<Stop> stops = originalFeed.getOrderedStopsForTrip(tripId);
    LineString originalShape = originalFeed.internal().getTripGeometry(tripId);
    LineString generatedShape = generatedFeed.internal().getTripGeometry(tripId);

    View view = new View()
        .add("trip", trip)
        .add("route", route)
        .add("stops", stops)
        .add("originalShape", originalShape)
        .add("generatedShape", generatedShape);

    return Response.ok(view).build();
  }

  private Comparator<ReportEntry> createComparator(SortQuery sortQuery) {
    Comparator<ReportEntry> comparator = null;
    switch (sortQuery.attribute()) {
      case "an":
        comparator = Comparator.comparingDouble(ReportEntry::an);
        break;
      case "al":
        comparator = Comparator.comparingDouble(ReportEntry::al);
        break;
      case "avgFd":
        comparator = Comparator.comparingDouble(ReportEntry::avgFd);
        break;
    }
    return comparator;
  }
}
