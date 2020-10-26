package de.fleigm.ptmm.eval;

import java.util.Objects;

public class Config {
  public final String profile;
  public final double candidateSearchRadius;
  public final double alpha;
  public final double beta;
  public final double uTurnDistancePenalty;

  public Config(String profile, double candidateSearchRadius, double alpha, double beta, double uTurnDistancePenalty) {
    this.profile = profile;
    this.candidateSearchRadius = candidateSearchRadius;
    this.alpha = alpha;
    this.beta = beta;
    this.uTurnDistancePenalty = uTurnDistancePenalty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Config)) return false;
    Config config = (Config) o;
    return Double.compare(config.candidateSearchRadius, candidateSearchRadius) == 0 &&
           Double.compare(config.alpha, alpha) == 0 &&
           Double.compare(config.beta, beta) == 0 &&
           Double.compare(config.uTurnDistancePenalty, uTurnDistancePenalty) == 0 &&
           Objects.equals(profile, config.profile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(profile, candidateSearchRadius, alpha, beta, uTurnDistancePenalty);
  }

  @Override
  public String toString() {
    return String.format(
        "Config{profile='%s', candidateSearchRadius=%s, alpha=%s, beta=%s, uTurnDistancePenalty=%s}",
        profile,
        candidateSearchRadius,
        alpha,
        beta,
        uTurnDistancePenalty);
  }
}
