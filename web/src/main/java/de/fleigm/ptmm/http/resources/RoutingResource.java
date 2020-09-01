package de.fleigm.ptmm.http.resources;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.RoutingService;
import de.fleigm.ptmm.routing.TransitRouter;
import de.fleigm.ptmm.http.views.RoutingView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("routing")
public class RoutingResource {

  private final Logger logger = LoggerFactory.getLogger(RoutingResource.class);

  @Inject
  TransitFeed transitFeed;

  @Inject
  GraphHopper hopper;

  private RoutingService routingService;

  @PostConstruct
  public void init() {
    routingService = new RoutingService(
        transitFeed,
        new TransitRouter(hopper, new PMap().putObject("profile", "bus_shortest")));
  }

  @GET
  @Path("{routeId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response routing(@PathParam("routeId") String routeId) {
    GTFSFeed feed = transitFeed.internal();

    if (!feed.routes.containsKey(routeId)) {
      return Response
          .status(Response.Status.NOT_FOUND)
          .entity("Route does not exist")
          .build();
    }

    Optional<Trip> trip = feed.trips.values()
        .stream()
        .filter(t -> t.route_id.equals(routeId))
        .findFirst();

    if (trip.isEmpty()) {
      return Response
          .status(Response.Status.NOT_FOUND)
          .entity("Could not find any trip for this route")
          .build();
    }

    List<Stop> stops = feed.getOrderedStopListForTrip(trip.get().trip_id)
        .stream()
        .map(feed.stops::get)
        .collect(Collectors.toList());


    RoutingResult routingResult = routingService.routeTrip(trip.get().trip_id);

    return Response.ok(new RoutingView(routingResult, stops)).build();
  }
}
