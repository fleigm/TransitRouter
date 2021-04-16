package de.fleigm.transitrouter;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

/**
 * Main class for the quarkus server application.
 */
public class ServerApplication implements QuarkusApplication {

  @Override
  public int run(String... args) throws Exception {
    Quarkus.waitForExit();
    return 0;
  }
}
