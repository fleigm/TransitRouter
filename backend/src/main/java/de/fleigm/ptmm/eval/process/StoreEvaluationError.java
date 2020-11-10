package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Info;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.BiConsumer;

@Slf4j
@Dependent
public class StoreEvaluationError implements BiConsumer<Info, Throwable> {
  private final String evaluationFolder;

  public StoreEvaluationError(@ConfigProperty(name = "evaluation.folder") String evaluationFolder) {
    this.evaluationFolder = evaluationFolder;
  }

  @Override
  public void accept(Info info, Throwable throwable) {
    File errorFile = info.fullPath(evaluationFolder).resolve("error.log").toFile();

    try (FileWriter fileWriter = new FileWriter(errorFile, true)) {
      fileWriter.write(throwable.toString());
      throwable.printStackTrace(new PrintWriter(fileWriter));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
