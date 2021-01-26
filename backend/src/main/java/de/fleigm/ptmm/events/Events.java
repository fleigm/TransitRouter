package de.fleigm.ptmm.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class Events {

  private Event<Object> eventBus;

  protected Events() {
  }

  @Inject
  public Events(Event<Object> eventBus) {
    this.eventBus = eventBus;
  }

  public <T> CompletionStage<T> fire(T event, Annotation... qualifiers) {
    Event<T> cdiEvent = eventBus.select((Class<T>) event.getClass(), qualifiers);

    cdiEvent.fire(event);
    return cdiEvent.fireAsync(event);
  }
}
