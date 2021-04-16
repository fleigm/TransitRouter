package de.fleigm.transitrouter.debug;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import de.fleigm.transitrouter.routing.RoutingResult;
import de.fleigm.transitrouter.routing.TransitRouter;
import de.fleigm.transitrouter.routing.TransitRouterMapMatching;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("debug/routing")
public class RoutingController {

  @Inject
  GraphHopper graphHopper;

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response route(@Valid @NotNull RoutingRequest request) {
    PMap routingParameters = new PMap()
        .putObject("profile", request.getProfile())
        .putObject("measurement_error_sigma", request.getSigma())
        .putObject("candidate_search_radius", request.getCandidateSearchRadius())
        .putObject("beta", request.getBeta());

    TransitRouter router = new TransitRouterMapMatching(graphHopper, routingParameters);

    RoutingResult route = router.route(request.getObservations());

    return Response.ok(route).build();
  }
}
