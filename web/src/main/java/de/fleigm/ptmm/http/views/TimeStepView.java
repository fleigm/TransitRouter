package de.fleigm.ptmm.http.views;

import com.bmw.hmm.Transition;
import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.util.TimeStep;
import com.graphhopper.routing.Path;

import java.util.List;
import java.util.stream.Collectors;

public class TimeStepView {
  public final Observation observation;
  public final List<StateView> candidates;
  public final List<EmissionProbability> emissionProbabilities;
  public final List<TransitionProbability> transitionProbabilities;

  public TimeStepView(TimeStep<State, Observation, Path> timeStep) {
    observation = timeStep.observation;

    candidates = timeStep.candidates.stream()
        .map(StateView::new)
        .collect(Collectors.toList());

    emissionProbabilities = timeStep.emissionLogProbabilities.entrySet().stream()
        .map(entry -> new EmissionProbability(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());

    transitionProbabilities = timeStep.transitionLogProbabilities.entrySet().stream()
        .map(entry -> new TransitionProbability(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }


  public static class StateView {
    public int id;
    public State state;

    public StateView(State state) {
      this.id = state.hashCode();
      this.state = state;
    }
  }

  public static class EmissionProbability {
    public final int state;
    public final double probability;

    public EmissionProbability(State state, double probability) {
      this.state = state.hashCode();
      this.probability = probability;
    }
  }

  public static class TransitionProbability {
    public final int fromState;
    public final int toState;
    public final double probability;

    public TransitionProbability(Transition<State> transition, double probability) {
      this.fromState = transition.fromCandidate.hashCode();
      this.toState = transition.toCandidate.hashCode();
      this.probability = probability;
    }
  }
}
