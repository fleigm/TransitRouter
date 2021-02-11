package de.fleigm.transitrouter.feeds.process;

import de.fleigm.transitrouter.feeds.GeneratedFeed;

public interface Step {

  void run(GeneratedFeed generatedFeed);
}
