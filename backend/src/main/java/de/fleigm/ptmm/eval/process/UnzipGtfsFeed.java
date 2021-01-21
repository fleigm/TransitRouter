package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.util.Unzip;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class UnzipGtfsFeed implements Consumer<GeneratedFeedInfo> {

  private final String evaluationFolder;

  public UnzipGtfsFeed(String evaluationFolder) {
    this.evaluationFolder = evaluationFolder;
  }

  @SneakyThrows
  @Override
  public void accept(GeneratedFeedInfo info) {
    log.info("Start unzip step.");

    Unzip.apply(
        info.getPath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FEED),
        info.getPath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FOLDER));

    log.info("Finished unzip step");
  }

}
