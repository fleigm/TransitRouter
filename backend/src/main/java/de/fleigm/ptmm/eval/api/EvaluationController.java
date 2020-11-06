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
    EvaluationResult evaluationResult = evaluationService.get(name);
    Report report = evaluationResult.report();

    ReportEntry highestAvgFd = report.entries()
        .stream()
        .max(Comparator.comparing(ReportEntry::avgFd))
        .get();

    ReportEntry lowestAvgFd = report.entries()
        .stream()
        .min(Comparator.comparing(ReportEntry::avgFd))
        .get();

    double averageAvgFd = report.entries()
        .stream()
        .mapToDouble(ReportEntry::avgFd)
        .average()
        .getAsDouble();

    double[] accuracies = report.accuracies();

    View view = new View()
        .add("highestAvgFd", new View()
            .add("tripId", highestAvgFd.tripId)
            .add("avgFd", highestAvgFd.avgFd)
            .add("details", getDetails(name, highestAvgFd.tripId)))
        .add("lowestAvgFd", new View()
            .add("tripId", lowestAvgFd.tripId)
            .add("avgFd", lowestAvgFd.avgFd)
            .add("details", getDetails(name, lowestAvgFd.tripId)))
        .add("averageAvgFd", averageAvgFd)
        .add("accuracies", accuracies);

    return Response.ok(view).build();
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

    EvaluationResult evaluationResult = evaluationService.get(name);
    Report report = evaluationResult.report();

    List<ReportEntry> entriesMatchingSearch = report.entries();
    if (!search.isBlank()) {
      entriesMatchingSearch = report.entries()
          .stream()
          .filter(reportEntry -> reportEntry.tripId.contains(search))
          .collect(Collectors.toList());
    }

    if (!sort.isBlank()) {
      Comparator<ReportEntry> comparator = createComparator(SortQuery.parse(sort));
      if (comparator != null) {
        if (SortQuery.parse(sort).order() == SortQuery.SortOrder.DESC) {
          comparator = comparator.reversed();
        }
        entriesMatchingSearch.sort(comparator);
      }
    }

    TransitFeed transitFeed = evaluationResult.generatedTransitFeed();

    List<View> entries = entriesMatchingSearch.stream()
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
        .setData(entries)
        .setCurrentPage(paged.getPage())
        .setPerPage(paged.getLimit())
        .setTotal(entriesMatchingSearch.size())
        .setUri(uriInfo.getAbsolutePath())
        .create();

    return Response.ok(page).build();
  }

  @GET
  @Path("{name}/trips/{tripId}")
  @Produces(MediaType.APPLICATION_JSON)
  public View getDetails(@PathParam("name") String name, @PathParam("tripId") String tripId) {
    EvaluationResult evaluationResult = evaluationService.get(name);
    TransitFeed originalFeed = evaluationResult.originalTransitFeed();
    TransitFeed generatedFeed = evaluationResult.generatedTransitFeed();

    Trip trip = generatedFeed.internal().trips.get(tripId);
    Route route = generatedFeed.getRouteForTrip(tripId);
    List<Stop> stops = originalFeed.getOrderedStopsForTrip(tripId);
    LineString originalShape = originalFeed.internal().getTripGeometry(tripId);
    LineString generatedShape = generatedFeed.internal().getTripGeometry(tripId);

    return new View()
        .add("trip", trip)
        .add("route", route)
        .add("stops", stops)
        .add("originalShape", originalShape)
        .add("generatedShape", generatedShape);
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
