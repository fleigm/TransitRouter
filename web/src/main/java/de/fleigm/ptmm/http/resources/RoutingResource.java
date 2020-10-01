package de.fleigm.ptmm.http.resources;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.graphhopper.GraphHopper;
import com.graphhopper.matching.Observation;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.GHPoint;
import com.vividsolutions.jts.geom.LineString;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.TransitRouter;
import de.fleigm.ptmm.http.views.RoutingView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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

  TransitRouter transitRouter;

  @PostConstruct
  public void init() {
    transitRouter = new TransitRouter(hopper, new PMap().putObject("profile", "bus_fastest"));
  }

  @GET
  @Path("{routeId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response routing(@PathParam("routeId") String routeId, @Context UriInfo uriInfo) {

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

    LineString originalShape = feed.getTripGeometry(trip.get().trip_id);

    List<Observation> observations = stops.stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());

    RoutingResult routingResult = transitRouter.route(observations);

    RoutingView view = new RoutingView(routingResult, stops, originalShape);
    return Response.ok(view).build();
  }
}
