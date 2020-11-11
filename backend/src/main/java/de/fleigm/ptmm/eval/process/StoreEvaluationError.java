package de.fleigm.ptmm.eval.process;

import de.fleigm.ptmm.eval.Info;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
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
    info.addExtension("error.message", ExceptionUtils.getMessage(throwable))
        .addExtension("error.stackTrace", ExceptionUtils.getStackTrace(throwable))
        .addExtension("error.rootCause", ExceptionUtils.getRootCauseMessage(throwable));
  }
}
