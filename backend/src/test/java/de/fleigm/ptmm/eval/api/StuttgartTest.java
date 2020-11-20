package de.fleigm.ptmm.eval.api;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.Observation;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.ptmm.Shape;
import de.fleigm.ptmm.ShapeGenerator;
import de.fleigm.ptmm.TransitFeed;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.routing.RoutingResult;
import de.fleigm.ptmm.routing.TransitRouter;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
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
  void run_evaluation() throws IOException, ExecutionException, InterruptedException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, "bus_shortest_turn").toFile());
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart_bus_only.zip").toFile();

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name("bus_shortest_turn")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile("bus_shortest_turn")
        .build();

    CompletableFuture<Info> result = evaluationService.createEvaluation(request);

    Info info = result.get();

    assertNotNull(info);
  }

  @ParameterizedTest
  @ValueSource(strings = {"bus_fastest", "bus_fastest_turn", "bus_shortest", "bus_shortest_turn"})
  void run_all_evaluations(String profile) throws IOException, ExecutionException, InterruptedException {
    FileUtils.deleteDirectory(Paths.get(evaluationFolder, profile).toFile());
    File testFeed = Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart_bus_only.zip").toFile();

    CreateEvaluationRequest request = CreateEvaluationRequest.builder()
        .name(profile)
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .sigma(25.0)
        .candidateSearchRadius(25.0)
        .beta(2.0)
        .profile(profile)
        .build();

    CompletableFuture<Info> result = evaluationService.createEvaluation(request);

    Info info = result.get();

    assertNotNull(info);
  }

  @Test
  void qwe() {
    String trip = "37.T0.31-814-j20-4.3.R";
    TransitFeed transitFeed = new TransitFeed(Paths.get(homeDir, "/uni/bachelor/project/files/stuttgart_bus_only.zip"));
    TransitRouter transitRouter = new TransitRouter(graphHopper, new PMap()
        .putObject("profile", "bus_fastest")
        .putObject("measurement_error_sigma", 25)
        .putObject("candidate_search_radius", 25)
        .putObject("beta", 2.0)
        .putObject("u_turn_distance_penalty", 1500));
    ShapeGenerator shapeGenerator = new ShapeGenerator(transitRouter);

    List<Observation> observations = transitFeed.getOrderedStopsForTrip(trip).stream()
        .map(stop -> new GHPoint(stop.stop_lat, stop.stop_lon))
        .map(Observation::new)
        .collect(Collectors.toList());

    Shape shape = shapeGenerator.generate(observations);

    assertNotNull(shape);
  }

  @Test
  void grtrt() {
    List<Observation> observations = List.of(
        new Observation(new GHPoint(48.6658586029462, 9.17788021004512)),
        new Observation(new GHPoint(48.6594163647239, 9.18581192889257)),
        new Observation(new GHPoint(48.6579892237722, 9.18967519598556)),
        new Observation(new GHPoint(48.6567412309377, 9.193878269348)),
        new Observation(new GHPoint(48.6539393417297, 9.19693492322262)),
        new Observation(new GHPoint(48.6490581944514, 9.19587086103749)),
        new Observation(new GHPoint(48.6471830126747, 9.19335296297837)),
        new Observation(new GHPoint(48.6474268060521, 9.18732839557161)),
        new Observation(new GHPoint(48.6463482135936, 9.19774665643316)),
        new Observation(new GHPoint(48.6477515228735, 9.20263773370914)),
        new Observation(new GHPoint(48.6506964237527, 9.21023622125165)),
        new Observation(new GHPoint(48.6525312470288, 9.21491275019464)),
        new Observation(new GHPoint(48.654848789912, 9.21626634921797)),
        new Observation(new GHPoint(48.6595431340697, 9.22087456695226)),
        new Observation(new GHPoint(48.6734401740607, 9.2190209800027)),
        new Observation(new GHPoint(48.6767424738456, 9.21794902762902))
    );

    TransitRouter transitRouter = new TransitRouter(graphHopper, new PMap()
        .putObject("profile", "bus_fastest")
        .putObject("measurement_error_sigma", 25)
        .putObject("candidate_search_radius", 25)
        .putObject("beta", 2.0));

    RoutingResult route = transitRouter.route(observations);

    assertNotNull(route);

  }
}
