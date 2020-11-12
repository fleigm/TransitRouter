package de.fleigm.ptmm.routing;

import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.Distributions;
import com.graphhopper.matching.TimeStep;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.util.PMap;

public class GraphhopperEmissionProbability implements EmissionProbability {
  private static final double DEFAULT_MEASUREMENT_ERROR_SIGMA = 25;

  private final double measurementErrorSigma;

  public GraphhopperEmissionProbability() {
    this(DEFAULT_MEASUREMENT_ERROR_SIGMA);
  }

  public GraphhopperEmissionProbability(double transitionBetaProbability) {
    this.measurementErrorSigma = transitionBetaProbability;
  }

  public static GraphhopperEmissionProbability create(PMap parameters) {
    return new GraphhopperEmissionProbability(
        parameters.getDouble("measurement_error_sigma", DEFAULT_MEASUREMENT_ERROR_SIGMA));
  }

  public double getMeasurementErrorSigma() {
    return measurementErrorSigma;
  }

  @Override
  public double calc(TimeStep<State, Observation, Path> timeStep, State candidate, QueryGraph queryGraph) {
    double distance = candidate.getSnap().getQueryDistance();

    return Distributions.logNormalDistribution(measurementErrorSigma, distance);
  }

  @Override
  public String toString() {
    return String.format("GH distance. Params: sigma={%f}", measurementErrorSigma);
  }
}
