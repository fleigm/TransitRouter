package de.fleigm.ptmm.eval.api;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.Shape;
import de.fleigm.ptmm.ShapeGenerator;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.routing.TransitRouter;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class StuttgartTest {

  @Inject
  EvaluationService evaluationService;

  @ConfigProperty(name = "user.home")
  String homeDir;

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  @Inject
  GraphHopper graphHopper;

  @Test
  void asd() throws IOException, ExecutionException, InterruptedException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, "st_test").toFile());
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart.zip").toFile();

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("st_test")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .alpha(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .uTurnDistancePenalty(1500.0)
        .profile("bus_custom_shortest")
        .build();

    CompletableFuture<Info> result = evaluationService.createEvaluation(request);

    Info info = result.get();

    assertNotNull(info);
  }

  @Test
  void qwe() {
    String trip = "37.T0.31-814-j20-4.3.R";
    TransitFeed transitFeed = new TransitFeed(Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart.zip"));
    TransitRouter transitRouter = new TransitRouter(graphHopper, new PMap()
        .putObject("profile", "bus_custom_shortest")
        .putObject("measurement_error_sigma", 25)
        .putObject("candidate_search_radius", 25)
        .putObject("beta", 2.0)
        .putObject("u_turn_distance_penalty", 1500));
    ShapeGenerator shapeGenerator = new ShapeGenerator(transitFeed, transitRouter);

    List<GHPoint> collect = transitFeed.getOrderedStopsForTrip(trip).stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .collect(Collectors.toList());

    Shape shape = shapeGenerator.generate(transitFeed.trips().get(trip));

    assertNotNull(shape);
  }
}
