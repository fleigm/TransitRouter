package de.fleigm.transitrouter.routing;

import com.bmw.hmm.Transition;
import com.graphhopper.routing.Path;

import java.util.HashMap;
import java.util.Map;

class HMMStep {

  private final Map<DirectedCandidate, Double> emissionProbabilities;
  private final Map<Transition<DirectedCandidate>, Double> transitionProbabilities;
  private final Map<Transition<DirectedCandidate>, Path> roadPaths;

  public HMMStep() {
    emissionProbabilities = new HashMap<>();
    transitionProbabilities = new HashMap<>();
    roadPaths = new HashMap<>();
  }

  public void addEmissionProbability(DirectedCandidate candidate, double probability) {
    emissionProbabilities.put(candidate, probability);
  }

  public void addTransitionProbability(DirectedCandidate from, DirectedCandidate to, double probability) {
    transitionProbabilities.put(new Transition<>(from, to), probability);
  }

  public void addRoadPath(DirectedCandidate from, DirectedCandidate to, Path path) {
    roadPaths.put(new Transition<>(from, to), path);
  }

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
