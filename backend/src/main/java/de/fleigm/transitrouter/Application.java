package de.fleigm.transitrouter;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;

public class Application implements QuarkusApplication {

  @Override
  public int run(String... args) throws Exception {
    System.out.println("Do startup logic here");
    Quarkus.waitForExit();
    return 0;
  }
}
