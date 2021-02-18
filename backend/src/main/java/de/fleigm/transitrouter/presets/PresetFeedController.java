package de.fleigm.transitrouter.presets;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import de.fleigm.transitrouter.Pattern;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.feeds.api.TransitRouterFactory;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.TransitFeedService;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.http.ResourcePageBuilder;
import de.fleigm.transitrouter.http.pagination.Page;
import de.fleigm.transitrouter.http.pagination.Paged;
import de.fleigm.transitrouter.http.search.SearchCriteria;
import de.fleigm.transitrouter.http.search.SearchQuery;
import de.fleigm.transitrouter.http.sort.SortQuery;
import de.fleigm.transitrouter.http.views.View;
import de.fleigm.transitrouter.routing.Observation;
import de.fleigm.transitrouter.routing.RoutingResult;
import de.fleigm.transitrouter.routing.TransitRouter;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("presets/{id}/feed")
public class PresetFeedController {

  @Inject
  PresetRepository presetRepository;

  @Inject
  TransitFeedService transitFeedService;

  @Inject
  CachedPatternService cachedPatternService;

  @Inject
  TransitRouterFactory transitRouterFactory;

  @GET
  public Response index(@PathParam("id") UUID id,
                        @Context UriInfo uriInfo,
                        @BeanParam Paged paged,
                        @QueryParam("search") @DefaultValue("") String search,
                        @QueryParam("sort") @DefaultValue("_none_:asc") String sort) {

    Preset preset = presetRepository.findOrFail(id);

    List<Pattern> patterns = cachedPatternService.getPatternsForFeed(preset.getFeed());

    ResourcePageBuilder<Pattern> resultBuilder = new ResourcePageBuilder<Pattern>(
        paged,
        SearchQuery.parse(search),
        SortQuery.parse(sort.isBlank() ? "_none_:asc" : sort),
        uriInfo
    )
        .add("type", this::typeFilter)
        .add(SearchCriteria.WILDCARD_KEY, this::nameFilter)
        .add("name", this::nameFilter);

    Page<Pattern> page = resultBuilder.build(patterns);

    return Response.ok(page).build();
  }

  private boolean typeFilter(SearchCriteria searchCriteria, Pattern pattern) {
    return Type.create(searchCriteria.intValue()).contains(pattern.route().route_type);
  }

  private boolean nameFilter(SearchCriteria searchCriteria, Pattern pattern) {
    return pattern.route().route_short_name.contains(searchCriteria.value())
           || pattern.route().route_long_name.contains(searchCriteria.value());
  }


  @GET
  @Path("{tripId}")
  public Response get(@PathParam("id") UUID id, @PathParam("tripId") String tripId) {
    Preset preset = presetRepository.findOrFail(id);
    TransitFeed transitFeed = transitFeedService.get(preset.getFeed().getPath());

    Trip trip = transitFeed.trips().get(tripId);
    Route route = transitFeed.routes().get(trip.route_id);
    List<Stop> stops = transitFeed.getOrderedStopsForTrip(trip);

    Parameters parameters = Parameters.defaultParameters();
    if (route.route_type != 3) {
      parameters.setProfile("rail");
    }

    TransitRouter router = transitRouterFactory.create(parameters);

    List<Observation> observations = stops.stream()
        .map(stop -> Observation.of(stop.stop_lat, stop.stop_lon))
        .collect(Collectors.toList());

    RoutingResult routing = router.route(observations);

    View view = new View()
        .add("trip", trip)
        .add("route", route)
        .add("stops", stops)
        .add("originalShape", transitFeed.internal().getTripGeometry(tripId))
        .add("generatedShape", routing.getPath());


    return Response.ok(view).build();

  }
}
