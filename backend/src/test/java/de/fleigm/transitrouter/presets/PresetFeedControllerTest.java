package de.fleigm.transitrouter.presets;

import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Trip;
import de.fleigm.transitrouter.gtfs.Feed;
import de.fleigm.transitrouter.gtfs.TransitFeed;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasKey;

@QuarkusTest
class PresetFeedControllerTest {

  @Inject
  PresetRepository presets;

  private Preset preset;
  private TransitFeed transitFeed;


  @BeforeEach
  void beforeEach() throws FileNotFoundException {
    if (preset != null) {
      return;
    }

    File testFeed = new File(getClass().getClassLoader().getResource("test_feed.zip").getFile());
    preset = Preset.builder()
        .name("test")
        .build();
    preset.setFeed(Feed.create(preset.getFileStoragePath().resolve("gtfs.zip"), new FileInputStream(testFeed)));
    presets.save(preset);

    transitFeed = new TransitFeed(preset.getFeed().getPath());
  }

  @Test
  void get_feed() {
    given().when()
        .get(String.format("presets/%s/feed", preset.getId()))
        .then()
        .statusCode(200)
        .body("total", Matchers.greaterThan(0));

    given().when()
        .get(String.format("presets/%s/feed", "unknown"))
        .then()
        .statusCode(404);
  }

  @Test
  void get_trip() {
    Route route = transitFeed.busRoutes().values().stream().findFirst().get();
    Trip trip = transitFeed.trips().values().stream().filter(t -> t.route_id.equals(route.route_id)).findFirst().get();

    given()
        .when()
        .get(String.format("presets/%s/feed/%s", preset.getId(), trip.trip_id))
        .then()
        .statusCode(200)
        .body("", hasKey("trip"))
        .body("", hasKey("route"))
        .body("", hasKey("stops"))
        .body("", hasKey("originalShape"))
        .body("", hasKey("generatedShape"));

    given().when()
        .get(String.format("presets/%s/feed/%s", "unknown", trip.trip_id))
        .then()
        .statusCode(404);

    given().when()
        .get(String.format("presets/%s/feed/%s", preset.getId(), "unknown"))
        .then()
        .statusCode(404);
  }
}