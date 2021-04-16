package de.fleigm.transitrouter.feeds.process;

import de.fleigm.transitrouter.feeds.GeneratedFeed;

/**
 * Process step
 */
public interface Step {

  void run(GeneratedFeed generatedFeed);
}
