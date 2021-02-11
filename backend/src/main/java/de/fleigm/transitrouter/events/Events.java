package de.fleigm.transitrouter.events;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;

/**
 * This class provides an abstraction over the CDI event system.
 * Every event will be fired as an CDI event both sync and async.
 */
@ApplicationScoped
public class Events {

  private Event<Object> eventBus;

  protected Events() {
  }

  @Inject
  public Events(Event<Object> eventBus) {
    this.eventBus = eventBus;
  }

  /**
   * Fire an event with the given annotations.
   * The event fill be fired as a synchronous and asynchronous CDI event.
   *
   * @param event      event to be fired.
   * @param qualifiers CDI Qualifiers
   * @param <T>        event type.
   * @return CompletionStage for async events.
   */
  public <T> CompletionStage<T> fire(T event, Annotation... qualifiers) {
    Event<T> cdiEvent = eventBus.select((Class<T>) event.getClass(), qualifiers);

    cdiEvent.fire(event);
    return cdiEvent.fireAsync(event);
  }
}
