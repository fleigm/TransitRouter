package de.fleigm.transitrouter.feeds.process;

import de.fleigm.transitrouter.feeds.Error;
import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.feeds.Status;
import de.fleigm.transitrouter.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handles the feed generation process.
 * Applies process steps, handles persistence and process failures.
 */
public class Process {
  private static final Logger LOGGER = LoggerFactory.getLogger(Process.class);

  private final GeneratedFeedRepository generatedFeedRepository;
  private final List<Step> processSteps = new ArrayList<>();

  public Process(GeneratedFeedRepository generatedFeedRepository) {
    this.generatedFeedRepository = generatedFeedRepository;
  }

  /**
   * Add a process step that will be applied when calling {@link Process#run(GeneratedFeed)}.
   *
   * @param step process step
   * @return this.
   */
  public Process addStep(Step step) {
    this.processSteps.add(step);

    return this;
  }

  /**
   * Apply the process steps to the given {@link GeneratedFeed} in the order they where added.
   * The {@link GeneratedFeed} is saved and the status will be set correctly.
   */
  public void run(GeneratedFeed generatedFeed) {
    MDC.put("evaluation.id", generatedFeed.getId().toString());
    MDC.put("evaluation.name", generatedFeed.getName());

    LOGGER.info("Start feed generation process.");

    StopWatch stopWatch = StopWatch.createAndStart();

    try {
      processSteps.forEach(step -> step.run(generatedFeed));
      generatedFeed.setStatus(Status.FINISHED);
    } catch (Throwable error) {
      LOGGER.error("feed generation failed.", error);
      generatedFeed.setStatus(Status.FAILED);
      generatedFeed.addError(
          Error.of("process.failed", "feed generation process failed.", error));
    } finally {
      stopWatch.stop();
      MDC.clear();

      generatedFeed
          .getOrCreateExtension(ExecutionTime.class, ExecutionTime::new)
          .add("total", Duration.of(stopWatch.getNanos(), ChronoUnit.NANOS));

      generatedFeedRepository.save(generatedFeed);

      LOGGER.info("Finished feed generation process. Took {}s", stopWatch.getSeconds());
    }
  }
  
  public CompletableFuture<Void> runAsync(GeneratedFeed generatedFeed) {
    return CompletableFuture.runAsync(() -> run(generatedFeed));
  }
}
