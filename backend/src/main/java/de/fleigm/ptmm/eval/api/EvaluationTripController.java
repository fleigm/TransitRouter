package de.fleigm.ptmm.eval.api;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.Trip;
import com.vividsolutions.jts.geom.LineString;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.EvaluationResult;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import de.fleigm.ptmm.http.views.View;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("eval/{id}/trips")
public class EvaluationTripController {

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @GET
  @Path("{tripId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response show(@PathParam("id") UUID id, @PathParam("tripId") String tripId) {
    Optional<EvaluationResult> evaluationResult = generatedFeedRepository.findEvaluationResult(id);

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
}
