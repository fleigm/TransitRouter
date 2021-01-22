package de.fleigm.ptmm.events;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class EventsTest {

  @Inject
  Events events;

  @Inject
  TestObserver observer;

  @BeforeEach
  void beforeEach() {
    observer.clear();
  }

  @Test
  void fire_events() {
    events.fire(new EventA());
    assertEquals(2, observer.observedEvents().size());
    assertTrue(observer.observedEvents().containsAll(List.of("base - A", "A - A")));

    observer.clear();
    events.fire(new EventA(), new CreatedQualifier());
    assertEquals(4, observer.observedEvents().size());
    assertTrue(observer.observedEvents().containsAll(List.of("base created - A", "base - A", "A created - A", "A - A")));

    observer.clear();
    events.fire(new EventA(), new DeletedQualifier());
    assertEquals(2, observer.observedEvents().size());
    assertTrue(observer.observedEvents().containsAll(List.of("base - A", "A - A")));
  }


}