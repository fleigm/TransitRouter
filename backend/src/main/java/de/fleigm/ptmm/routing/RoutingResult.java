package de.fleigm.ptmm.routing;

import com.graphhopper.matching.Observation;
import com.graphhopper.matching.State;
import com.graphhopper.matching.util.TimeStep;
import com.graphhopper.routing.Path;
import com.graphhopper.util.shapes.GHPoint;

import java.util.List;

public class RoutingResult {

  private Path path;
  private double distance;
  private double time;
  private List<Observation> observations;
  private List<GHPoint> candidates;

  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public double getTime() {
    return time;
  }

  public void setTime(double time) {
    this.time = time;
  }

  public List<Observation> getObservations() {
    return observations;
  }

  public void setObservations(List<Observation> observations) {
    this.observations = observations;
  }

  public List<GHPoint> getCandidates() {
    return candidates;
  }

  public void setCandidates(List<GHPoint> candidates) {
    this.candidates = candidates;
  }
}
