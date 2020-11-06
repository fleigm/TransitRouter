package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.Info;
import de.fleigm.ptmm.util.Unzip;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import java.util.function.Consumer;

@Dependent
public class UnzipGtfsFeed implements Consumer<Info> {

  private final String evaluationFolder;

  public UnzipGtfsFeed(@ConfigProperty(name = "evaluation.folder") String evaluationFolder) {
    this.evaluationFolder = evaluationFolder;
  }

  @SneakyThrows
  @Override
  public void accept(Info info) {
    Unzip.apply(
        info.fullPath(evaluationFolder).resolve(Evaluation.ORIGINAL_GTFS_FEED),
        info.fullPath(evaluationFolder).resolve(Evaluation.ORIGINAL_GTFS_FOLDER));

    Unzip.apply(
        info.fullPath(evaluationFolder).resolve(Evaluation.GENERATED_GTFS_FEED),
        info.fullPath(evaluationFolder).resolve(Evaluation.GENERATED_GTFS_FOLDER));
  }

}
