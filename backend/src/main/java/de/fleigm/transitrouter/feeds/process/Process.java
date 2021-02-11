package de.fleigm.transitrouter.feeds.process;

import de.fleigm.transitrouter.feeds.Error;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.util.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Process {

  private final GeneratedFeedRepository generatedFeedRepository;
  private final List<Step> processSteps = new ArrayList<>();

  public Process(GeneratedFeedRepository generatedFeedRepository) {
    this.generatedFeedRepository = generatedFeedRepository;
  }

  public Process addStep(Step step) {
    this.processSteps.add(step);

    return this;
  }

  public void run(GeneratedFeed generatedFeed) {
    MDC.put("evaluation.id", generatedFeed.getId().toString());
    MDC.put("evaluation.name", generatedFeed.getName());

    log.info("Start evaluation process.");

    StopWatch stopWatch = StopWatch.createAndStart();

    try {
      processSteps.forEach(step -> step.run(generatedFeed));
      generatedFeed.setStatus(Status.FINISHED);
    } catch (Throwable error) {
      log.error("Evaluation failed.", error);
      generatedFeed.setStatus(Status.FAILED);
      generatedFeed.addError(Error.of("process.failed", "Evaluation process failed.", error));
    } finally {
      stopWatch.stop();
      MDC.clear();

      generatedFeed
          .getOrCreateExtension(ExecutionTime.class, ExecutionTime::new)
          .add("total", Duration.of(stopWatch.getNanos(), ChronoUnit.NANOS));

      generatedFeedRepository.save(generatedFeed);

      log.info("Finished evaluation process. Took {}s", stopWatch.getSeconds());
    }
  }

  public CompletableFuture<Void> runAsync(GeneratedFeed generatedFeed) {
    return CompletableFuture.runAsync(() -> run(generatedFeed));
  }
}
