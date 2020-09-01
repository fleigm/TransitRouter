package de.fleigm.ptmm.http.resources;

import com.conveyal.gtfs.model.Trip;
import de.fleigm.ptmm.TransitFeed;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("gtfs")
public class GTFSFeedResource {

  @Inject
  TransitFeed feed;

  @GET
  @Path("routes")
  @Produces(MediaType.APPLICATION_JSON)
  public Response routes() {
    return Response.ok(feed.routes().values()).build();
  }

  @GET
  @Path("routes/{routeId}/trips")
  @Produces(MediaType.APPLICATION_JSON)
  public Response routeTrips(@PathParam("routeId") String routeId) {
    List<Trip> trips = feed.trips()
        .values()
        .stream()
        .filter(trip -> trip.route_id.equals(routeId))
        .collect(Collectors.toList());

    return Response.ok(trips).build();
  }
}
