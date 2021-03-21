package de.fleigm.transitrouter.feeds.evaluation;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.vividsolutions.jts.geom.LineString;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.TransitFeedService;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.http.ResourcePageBuilder;
import de.fleigm.transitrouter.http.pagination.Page;
import de.fleigm.transitrouter.http.pagination.Pagination;
import de.fleigm.transitrouter.http.search.SearchCriteria;
import de.fleigm.transitrouter.http.search.SearchQuery;
import de.fleigm.transitrouter.http.sort.SortQuery;
import de.fleigm.transitrouter.http.views.View;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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

@Path("feeds/{id}/trips")
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
    GeneratedFeed feed = generatedFeedRepository.findOrFail(id);

    if (!feed.getStatus().finished()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    TransitFeed originalFeed = transitFeedService.get(feed.getOriginalFeed().getPath());
    TransitFeed generatedFeed = transitFeedService.get(feed.getFeed().getPath());

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

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response index(@PathParam("id") UUID id,
                        @Context UriInfo uriInfo,
                        @BeanParam Pagination pagination,
                        @QueryParam("search") @DefaultValue("") String search,
                        @QueryParam("sort") @DefaultValue("") String sort) {

    GeneratedFeed info = generatedFeedRepository.findOrFail(id);

    if (!info.getStatus().finished()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    TransitFeed transitFeed = transitFeedService.get(info.getFeed().getPath());
    List<Entry> entries;

    ResourcePageBuilder<Entry> resultBuilder = new ResourcePageBuilder<Entry>()
        .path(uriInfo.getAbsolutePath())
        .pagination(pagination)
        .searchQuery(SearchQuery.parse(search))
        .sortQuery(SortQuery.parse(sort.isBlank() ? "_none_:asc" : sort))
        .addSearch("type", this::typeFilter)
        .addSearch("name", this::nameFilter)
        .addSearch(SearchCriteria.WILDCARD_KEY, this::wildCardFilter);

    Optional<Evaluation> evaluation = info.getExtension(Evaluation.class);
    if (evaluation.isPresent()) {
      Report report = reportService.get(evaluation.get().getReport());
      entries = report.entries()
          .stream()
          .map(entry -> Entry.create(entry, transitFeed))
          .collect(Collectors.toList());

      resultBuilder
          .addSort("an", Comparator.comparingDouble(Entry::getAn))
          .addSort("al", Comparator.comparingDouble(Entry::getAl))
          .addSort("avgFd", Comparator.comparingDouble(Entry::getAvgFd));
    } else {
      entries = transitFeed.trips()
          .keySet()
          .stream()
          .map(tripId -> Entry.create(tripId, transitFeed))
          .collect(Collectors.toList());
    }


    Page<Entry> page = resultBuilder.build(entries);

    return Response.ok(page).build();
  }

  private boolean typeFilter(SearchCriteria searchCriteria, Entry entry) {
    return entry.type.contains(searchCriteria.intValue());
  }

  private boolean nameFilter(SearchCriteria searchCriteria, Entry entry) {
    return entry.routeShortName.contains(searchCriteria.value())
           || entry.routeLongName.contains(searchCriteria.value());
  }

  private boolean wildCardFilter(SearchCriteria searchCriteria, Entry entry) {
    return entry.tripId.contains(searchCriteria.value())
           || entry.routeId.contains(searchCriteria.value())
           || entry.routeShortName.contains(searchCriteria.value())
           || entry.routeLongName.contains(searchCriteria.value());
  }


  @Getter
  @AllArgsConstructor
  @EqualsAndHashCode
  @ToString
  public static class Entry {
    public final String tripId;
    public final double an;
    public final double al;
    public final double avgFd;
    public final Type type;
    public final String routeId;
    public final String routeShortName;
    public final String routeLongName;

    public static Entry create(String tripId, TransitFeed transitFeed) {
      Route route = transitFeed.getRouteForTrip(tripId);
      return new Entry(
          tripId,
          0, 0, 0,
          Type.create(route.route_type),
          route.route_id,
          route.route_short_name,
          route.route_long_name
      );
    }

    public static Entry create(Report.Entry entry, TransitFeed transitFeed) {
      Route route = transitFeed.getRouteForTrip(entry.tripId);
      return new Entry(
          entry.tripId,
          entry.an,
          entry.al,
          entry.avgFd,
          Type.create(route.route_type),
          route.route_id,
          route.route_short_name,
          route.route_long_name

      );
    }
  }
}
