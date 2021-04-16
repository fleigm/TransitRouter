package de.fleigm.transitrouter.routing;

import com.bmw.hmm.Transition;
import com.graphhopper.routing.Path;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for storing the probabilities of a time step in an HMM
 */
class HMMStep {

  private final Map<DirectedCandidate, Double> emissionProbabilities;
  private final Map<Transition<DirectedCandidate>, Double> transitionProbabilities;
  private final Map<Transition<DirectedCandidate>, Path> roadPaths;

  public HMMStep() {
    emissionProbabilities = new HashMap<>();
    transitionProbabilities = new HashMap<>();
    roadPaths = new HashMap<>();
  }

  /**
   * add emission probability to a candidate.
   */
  public void addEmissionProbability(DirectedCandidate candidate, double probability) {
    emissionProbabilities.put(candidate, probability);
  }

  /**
   * add transition probability and path for a transition
   */
  public void addTransition(DirectedCandidate from, DirectedCandidate to, Path path, double probability) {
    Transition<DirectedCandidate> transition = new Transition<>(from, to);
    roadPaths.put(transition, path);
    transitionProbabilities.put(transition, probability);
  }

  public Map<DirectedCandidate, Double> emissionProbabilities() {
    return emissionProbabilities;
  }

  public Map<Transition<DirectedCandidate>, Double> transitionProbabilities() {
    return transitionProbabilities;
  }

  public Map<Transition<DirectedCandidate>, Path> roadPaths() {
    return roadPaths;
  }
}
