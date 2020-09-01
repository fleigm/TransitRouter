package de.fleigm.ptmm;

import de.fleigm.ptmm.commands.EntryCommand;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.inject.Inject;

@QuarkusMain
public class Application implements QuarkusApplication {
  public static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  @Inject
  CommandLine.IFactory factory;

  @Override
  public int run(String... args) throws Exception {
    return new CommandLine(new EntryCommand(), factory).execute(args);
  }

  public static void main(String... args) {
    Quarkus.run(Application.class, args);
  }
}
