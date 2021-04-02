package de.fleigm.transitrouter.feeds.evaluation;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Trip;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Parameters;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.feeds.api.FeedGenerationResponse;
import de.fleigm.transitrouter.feeds.api.FeedGenerationService;
import de.fleigm.transitrouter.feeds.api.GenerateFeedRequest;
import de.fleigm.transitrouter.feeds.evaluation.TripController.Entry;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import de.fleigm.transitrouter.gtfs.Type;
import de.fleigm.transitrouter.http.search.SearchCriteria;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class TripControllerTest {

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @Inject
  FeedGenerationService feedGenerationService;

  private GeneratedFeed generatedFeed;
  private TransitFeed transitFeed;

  @BeforeEach
  void beforeEach() throws IOException, InterruptedException, ExecutionException {
    if (generatedFeed != null) {
      return;
    }

    generatedFeed = generateFeed(true);
    transitFeed = new TransitFeed(generatedFeed.getFeed().getPath());
  }

  private GeneratedFeed generateFeed(boolean withEvaluation)
      throws IOException, InterruptedException, ExecutionException {

    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());

    GenerateFeedRequest request = GenerateFeedRequest.builder()
        .name("happy_path")
        .gtfsFeed(FileUtils.openInputStream(testFeed))
        .parameters(Type.BUS, Parameters.defaultParameters())
        .withEvaluation(withEvaluation)
        .build();
    FeedGenerationResponse response = feedGenerationService.create(request);

    synchronized (response.process()) {
      response.process().get();
    }

    assertTrue(response.process().isDone());

    return response.generatedFeed();
  }

  @Test
  void show_trip() {
    Route route = transitFeed.busRoutes().values().stream().findFirst().get();
    Trip trip = transitFeed.trips().values().stream().filter(t -> t.route_id.equals(route.route_id)).findFirst().get();

    given()
        .when()
        .get(String.format("feeds/%s/trips/%s", generatedFeed.getId(), trip.trip_id))
        .then()
        .statusCode(200)
        .body("", hasKey("trip"))
        .body("", hasKey("route"))
        .body("", hasKey("stops"))
        .body("", hasKey("originalShape"))
        .body("", hasKey("generatedShape"));
  }

  @Test
  void show_trip_not_found() {
    Route route = transitFeed.busRoutes().values().stream().findFirst().get();
    Trip trip = transitFeed.trips().values().stream().filter(t -> t.route_id.equals(route.route_id)).findFirst().get();

    given().when()
        .get(String.format("feeds/%s/trips/%s", "unknown", trip.trip_id))
        .then()
        .statusCode(404);

    given().when()
        .get(String.format("feeds/%s/trips/%s", generatedFeed.getId(), "unknown"))
        .then()
        .statusCode(404);

    GeneratedFeed pendingFeed = GeneratedFeed.builder().status(Status.PENDING).build();
    given().when()
        .get(String.format("feeds/%s/trips/%s", pendingFeed.getId(), trip.trip_id))
        .then()
        .statusCode(404);

    GeneratedFeed failedFeed = GeneratedFeed.builder().status(Status.FAILED).build();
    given().when()
        .get(String.format("feeds/%s/trips/%s", failedFeed.getId(), trip.trip_id))
        .then()
        .statusCode(404);
  }

  @Test
  void index() throws InterruptedException, ExecutionException, IOException {
    given().when()
        .get(String.format("feeds/%s/trips", generatedFeed.getId()))
        .then()
        .statusCode(200)
        .body("total", Matchers.greaterThan(0));

    GeneratedFeed feedWithoutEval = generateFeed(false);

    given().when()
        .get(String.format("feeds/%s/trips", feedWithoutEval.getId()))
        .then()
        .statusCode(200)
        .body("total", Matchers.greaterThan(0));

    given().when()
        .get(String.format("feeds/%s/trips", "unknown"))
        .then()
        .statusCode(404);

    GeneratedFeed pendingFeed = GeneratedFeed.builder().status(Status.PENDING).build();
    given().when()
        .get(String.format("feeds/%s/trips", pendingFeed.getId()))
        .then()
        .statusCode(404);

    GeneratedFeed failedFeed = GeneratedFeed.builder().status(Status.FAILED).build();
    given().when()
        .get(String.format("feeds/%s/trips", failedFeed.getId()))
        .then()
        .statusCode(404);
  }

  @Test
  void entry_search_filters() {
    Entry entry = new Entry(
        "tripId",
        0, 0, 0,
        Type.BUS,
        "routeId",
        "shortName",
        "longName");


    // search criteria is already applied to correct method
    // we do not care about key or operation at this point anymore
    // so we can use the wildcard factory method
    assertTrue(Entry.typeFilter(SearchCriteria.createWildcard("3"), entry));
    assertFalse(Entry.typeFilter(SearchCriteria.createWildcard("2"), entry));

    assertTrue(Entry.nameFilter(SearchCriteria.createWildcard("Name"), entry));
    assertTrue(Entry.nameFilter(SearchCriteria.createWildcard("short"), entry));
    assertTrue(Entry.nameFilter(SearchCriteria.createWildcard("long"), entry));
    assertTrue(Entry.nameFilter(SearchCriteria.createWildcard("long"), entry));
    assertFalse(Entry.nameFilter(SearchCriteria.createWildcard("test"), entry));

    assertTrue(Entry.wildCardFilter(SearchCriteria.createWildcard("Name"), entry));
    assertTrue(Entry.wildCardFilter(SearchCriteria.createWildcard("short"), entry));
    assertTrue(Entry.wildCardFilter(SearchCriteria.createWildcard("long"), entry));
    assertTrue(Entry.wildCardFilter(SearchCriteria.createWildcard("long"), entry));
    assertTrue(Entry.wildCardFilter(SearchCriteria.createWildcard("Id"), entry));
    assertTrue(Entry.wildCardFilter(SearchCriteria.createWildcard("trip"), entry));
    assertTrue(Entry.wildCardFilter(SearchCriteria.createWildcard("route"), entry));
    assertFalse(Entry.wildCardFilter(SearchCriteria.createWildcard("test"), entry));
  }
}