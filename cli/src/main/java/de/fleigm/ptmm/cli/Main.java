package de.fleigm.ptmm.cli;

import picocli.CommandLine;

public class Main {

  public static void main(String[] args) {
    int exitCode = new CommandLine(new CreateShapeFileCommand()).execute(args);
    System.exit(exitCode);
  }
}
