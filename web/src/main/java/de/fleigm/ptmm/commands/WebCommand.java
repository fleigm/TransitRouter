package de.fleigm.ptmm.commands;

import io.quarkus.runtime.Quarkus;
import picocli.CommandLine;

@CommandLine.Command(name = "web", description = "Start a web application")
public class WebCommand  implements Runnable {

  @Override
  public void run() {
    Quarkus.waitForExit();
  }
}
