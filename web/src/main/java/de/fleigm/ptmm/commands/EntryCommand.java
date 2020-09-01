package de.fleigm.ptmm.commands;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true,
    subcommands = {
        WebCommand.class,
        CreateShapeFileCommand.class
    })
public class EntryCommand {

  @CommandLine.Option(
      names = {"-o", "--osm-file"},
      paramLabel = "OSM_FILE",
      description = "OSM file")
  String osmFile;

  @CommandLine.Option(
      names = {"-g", "--gtfs-file"},
      paramLabel = "GTFS_FILE",
      description = "GTFS file")
  String gtfsFile;

  @CommandLine.Option(
      names = {"-t", "--temp"},
      paramLabel = "TEMP_FOLDER",
      description = "Folder for temporary files",
      defaultValue = "gh")
  String tempFolder;

  @CommandLine.Option(
      names = {"-c", "--clean"},
      paramLabel = "CLEAN",
      description = "Clean temporary files.")
  boolean clean;
}
