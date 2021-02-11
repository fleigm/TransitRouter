package de.fleigm.transitrouter.util;

import java.util.Objects;

/**
 * Helper class for time measurements.
 */
public class StopWatch {
  private long lastTime;
  private long elapsedTime;

  /**
   * Create an start a new {@link StopWatch}.
   *
   * @return started stop watch.
   */
  public static StopWatch createAndStart() {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    return stopWatch;
  }

  /**
   * Start the stop watch.
   */
  public void start() {
    elapsedTime = 0;
    lastTime = System.nanoTime();
  }

  /**
   * Stop the stop watch.
   */
  public void stop() {
    if (lastTime <= 0) {
      return;
    }

    elapsedTime += System.nanoTime() - lastTime;
  }

  /**
   * @return elapsed time in nano seconds.
   */
  public long getNanos() {
    return elapsedTime;
  }

  /**
   * @return elapsed time in milli seconds.
   */
  public long getMillis() {
    return elapsedTime / 1_000_000;
  }

  /**
   * @return elapsed time in seconds.
   */
  public float getSeconds() {
    return elapsedTime / 1e9f;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StopWatch)) return false;
    StopWatch stopWatch = (StopWatch) o;
    return lastTime == stopWatch.lastTime && elapsedTime == stopWatch.elapsedTime;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lastTime, elapsedTime);
  }
}
