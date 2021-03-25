package de.fleigm.transitrouter.commands;

import de.fleigm.transitrouter.ServerApplication;
import io.quarkus.runtime.Quarkus;
import picocli.CommandLine;

@CommandLine.Command(name = "serve", description = "Start the TransitRouter web application.")
public class StartServerCommand implements Runnable {

  @Override
  public void run() {
    Quarkus.run(ServerApplication.class);
  }
}
