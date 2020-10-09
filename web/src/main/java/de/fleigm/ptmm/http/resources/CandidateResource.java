package de.fleigm.ptmm.http.resources;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.Observation;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.TransitRouter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("candidates")
public class CandidateResource {

  @Inject
  GraphHopper hopper;

  TransitRouter transitRouter;

  @PostConstruct
  public void init() {
    transitRouter = new TransitRouter(hopper, new PMap().putObject("profile", "bus_fastest"));
  }

  @GET
  public Response findCandidates(
      @QueryParam("lat") double latitude,
      @QueryParam("lon") double longitude,
      @QueryParam("radius") double radius) {

    LocationIndexTree locationIndex = (LocationIndexTree) hopper.getLocationIndex();

    FlagEncoder flagEncoder = hopper.getEncodingManager().getEncoder("bus");

    List<QueryResult> candidates = locationIndex.findNClosest(
        latitude,
        longitude,
        DefaultEdgeFilter.allEdges(flagEncoder),
        radius);

    //List<QueryResult> candidates = transitRouter.getCandidates(new Observation(new GHPoint(latitude, longitude)));

    List<GHPoint3D> result = candidates.stream().map(QueryResult::getSnappedPoint).collect(Collectors.toList());

    return Response.ok(result).build();
  }
}
