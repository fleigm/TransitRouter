package de.fleigm.ptmm.eval.api;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.vividsolutions.jts.geom.LineString;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.Status;
import de.fleigm.ptmm.feeds.TransitFeedService;
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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("eval/{id}/trips")
public class EvaluationTripController {

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  TransitFeedService transitFeedService;

  @Inject
  ReportService reportService;

  @GET
  @Path("{tripId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response show(@PathParam("id") UUID id, @PathParam("tripId") String tripId) {

    return generatedFeedRepository.find(id)
        .filter(GeneratedFeedInfo::hasFinished)
        .map(info -> {
          TransitFeed originalFeed = transitFeedService.get(info.getOriginalFeed().getPath());
          TransitFeed generatedFeed = transitFeedService.get(info.getGeneratedFeed().getPath());

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

          return Response.ok(view);
        })
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();


  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index(@PathParam("id") UUID id,
                        @Context UriInfo uriInfo,
                        @BeanParam Paged paged,
                        @QueryParam("search") @DefaultValue("") String search,
                        @QueryParam("sort") @DefaultValue("") String sort) {

    GeneratedFeedInfo info = generatedFeedRepository.findOrFail(id);

    if (info.getStatus() != Status.FINISHED) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    EvaluationExtension evaluation = info.getExtension(EvaluationExtension.class).get();
    Report report = reportService.get(evaluation.getReport());
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

    TransitFeed transitFeed = transitFeedService.get(info.getGeneratedFeed().getPath());

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
