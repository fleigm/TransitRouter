package de.fleigm.ptmm.http.eval;

import com.conveyal.gtfs.model.Stop;
import com.vividsolutions.jts.geom.LineString;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.ReportEntry;
import de.fleigm.ptmm.http.TransitFeedService;
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
import java.util.stream.Collectors;

@Path("eval/{name}")
public class EvaluationResource {

  @Inject
  Reports reports;

  @Inject
  TransitFeedService transitFeedService;

  @PathParam("name")
  String name;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    Report report = reports.get(name);

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
            .add("details", getDetails(highestAvgFd.tripId)))
        .add("lowestAvgFd", new View()
            .add("tripId", lowestAvgFd.tripId)
            .add("avgFd", lowestAvgFd.avgFd)
            .add("details", getDetails(lowestAvgFd.tripId)))
        .add("averageAvgFd", averageAvgFd)
        .add("accuracies", accuracies);

    return Response.ok(view).build();
  }


  @GET
  @Path("trips")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTrips(
      @Context UriInfo uriInfo,
      @BeanParam Paged paged,
      @QueryParam("search") @DefaultValue("") String search,
      @QueryParam("sort") @DefaultValue("") String sort) {

    Report report = reports.get(name);

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

    TransitFeed transitFeed = transitFeedService.get("../../../" + name + "/gtfs.generated.zip");

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
  @Path("trips/{tripId}")
  @Produces(MediaType.APPLICATION_JSON)
  public View getDetails(@PathParam("tripId") String tripId) {
    TransitFeed originalFeed = transitFeedService.get("../../../" + name + "/gtfs.original.zip");
    TransitFeed generatedFeed = transitFeedService.get("../../../" + name + "/gtfs.generated.zip");

    List<Stop> stops = originalFeed.getOrderedStopsForTrip(tripId);
    LineString originalShape = originalFeed.internal().getTripGeometry(tripId);
    LineString generatedShape = generatedFeed.internal().getTripGeometry(tripId);

    return new View()
        .add("tripId", tripId)
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
