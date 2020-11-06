package de.fleigm.ptmm;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.PointList;

import java.util.Arrays;

public class FrechetDistance {
  private static final DistanceCalc distanceCalc = new DistanceCalcEarth();

  private final double[][] memory;
  private final PointList a;
  private final PointList b;

  public FrechetDistance(PointList a, PointList b) {
    this.a = a;
    this.b = b;

    this.memory = new double[a.size()][b.size()];
    for (double[] doubles : memory) {
      Arrays.fill(doubles, -1.0);
    }
  }

  public static double compute(PointList a, PointList b) {
    return new FrechetDistance(a, b).computeDFD();
  }

  public static double computeRecursive(PointList a, PointList b) {
    return new FrechetDistance(a, b).computeRecursiveDFD(a.size() - 1, b.size() - 1);
  }

  public double computeRecursiveDFD(int i, int j) {
    if (memory[i][j] > -1.0) {
      return memory[i][j];
    } else if (i == 0 && j == 0) {
      memory[i][j] = calcDistance(i, j);
    } else if (i > 0 && j == 0) {
      memory[i][j] = Math.max(memory[i - 1][j], calcDistance(i, j));
    } else if (i == 0 && j > 0) {
      memory[i][j] = Math.max(memory[i][j - 1], calcDistance(i, j));
    } else {
      memory[i][j] = Math.max(Math.min(Math.min(memory[i - 1][j - 1], memory[i - 1][j]), memory[i][j - 1]), calcDistance(i, j));
    }

    return memory[i][j];
  }

  public double computeDFD() {
    memory[0][0] = calcDistance(0, 0);

    for (int i = 1; i < a.size(); i++) {
      memory[i][0] = Math.max(memory[i - 1][0], calcDistance(i, 0));
    }

    for (int j = 1; j < b.size(); j++) {
      memory[0][j] = Math.max(memory[0][j - 1], calcDistance(0, j));
    }

    for (int i = 1; i < a.size(); i++) {
      for (int j = 1; j < b.size(); j++) {
        memory[i][j] = Math.max(Math.min(Math.min(memory[i - 1][j - 1], memory[i - 1][j]), memory[i][j - 1]), calcDistance(i, j));
      }
    }

    return memory[a.size() - 1][b.size() - 1];
  }

  private double calcDistance(int i, int j) {
    return distanceCalc.calcDist(a.getLat(i), a.getLon(i), b.getLat(j), b.getLon(j));
  }
}
