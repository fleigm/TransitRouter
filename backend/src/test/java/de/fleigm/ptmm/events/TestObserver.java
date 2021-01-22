package de.fleigm.ptmm.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TestObserver {

  private List<String> observedEvents = new ArrayList<>();

  public void clear() {
    observedEvents.clear();
  }

  public List<String> observedEvents() {
    return observedEvents;
  }

  public void observeAll(@Observes BaseEvent event) {
    observedEvents.add("base - " + event.message());
  }

  public void observeAllWithQualifier(@Observes @Created BaseEvent event) {
    observedEvents.add("base created - " + event.message());
  }

  public void observeA(@Observes EventA event) {
    observedEvents.add("A - " + event.message());
  }

  public void observeAWithQualifier(@Observes @Created EventA event) {
    observedEvents.add("A created - " + event.message());
  }

  public void observeB(@Observes EventB event) {
    observedEvents.add("B - " + event.message());
  }

  public void observeBWithQualifier(@Observes @Created EventB event) {
    observedEvents.add("B created" + event.message());
  }
}
