package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.util.Unzip;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class UnzipGtfsFeed implements Consumer<Info> {

  private final String evaluationFolder;

  public UnzipGtfsFeed(String evaluationFolder) {
    this.evaluationFolder = evaluationFolder;
  }

  @SneakyThrows
  @Override
  public void accept(Info info) {
    log.info("Start unzip step.");

    Unzip.apply(
        info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FEED),
        info.getPath().resolve(Evaluation.ORIGINAL_GTFS_FOLDER));

    Unzip.apply(
        info.getPath().resolve(Evaluation.GENERATED_GTFS_FEED),
        info.getPath().resolve(Evaluation.GENERATED_GTFS_FOLDER));

    log.info("Finished unzip step");
  }

}
