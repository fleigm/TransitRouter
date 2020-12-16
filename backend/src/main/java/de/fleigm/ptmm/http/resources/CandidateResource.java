package de.fleigm.ptmm.http.resources;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.routing.DefaultTransitRouter;
import de.fleigm.ptmm.routing.TransitRouter;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("candidates")
public class CandidateResource {

  @Inject
  GraphHopper hopper;

  TransitRouter transitRouter;

  @PostConstruct
  public void init() {
    transitRouter = new DefaultTransitRouter(hopper, new PMap().putObject("profile", "bus_fastest"));
  }

  @GET
  public Response findCandidates(
      @QueryParam("lat") double latitude,
      @QueryParam("lon") double longitude,
      @QueryParam("radius") double radius) {

    LocationIndexTree locationIndex = (LocationIndexTree) hopper.getLocationIndex();

    FlagEncoder flagEncoder = hopper.getEncodingManager().getEncoder("bus");

    List<Snap> candidates = locationIndex.findNClosest(
        latitude,
        longitude,
        DefaultEdgeFilter.allEdges(flagEncoder),
        radius);

    QueryGraph graph = QueryGraph.create(hopper.getGraphHopperStorage(), candidates);

    List<Candidate> c = new ArrayList<>();

    for (Snap candidate : candidates) {
      List<PointList> directions = new ArrayList<>();
      EdgeIterator edgeIterator = graph.createEdgeExplorer().setBaseNode(candidate.getClosestNode());
      while (edgeIterator.next()) {
        EdgeIteratorState edge = graph.getEdgeIteratorState(edgeIterator.getEdge(), edgeIterator.getAdjNode());
        PointList points = edge.fetchWayGeometry(FetchMode.ALL);
        directions.add(points);
      }
      c.add(new Candidate(candidate.getSnappedPoint(), directions));
    }

    return Response.ok(c).build();
  }

  @Data
  @AllArgsConstructor
  private static class Candidate {
    private GHPoint point;
    private List<PointList> directions;
  }
}
