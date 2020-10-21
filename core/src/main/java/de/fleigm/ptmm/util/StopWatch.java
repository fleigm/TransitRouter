package de.fleigm.ptmm.util;

public class StopWatch {
  private long lastTime;
  private long elapsedTime;

  public static StopWatch createAndStart() {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    return stopWatch;
  }


  public void start() {
    elapsedTime = 0;
    lastTime = System.nanoTime();
  }

  public void stop() {
    if (lastTime <= 0) {
      return;
    }

    elapsedTime += System.nanoTime() - lastTime;
  }

  public long getNanos() {
    return elapsedTime;
  }

  public long getMillis() {
    return elapsedTime / 1_000_000;
  }

  public float getSeconds() {
    return elapsedTime / 1e9f;
  }
}
