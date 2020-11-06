package de.fleigm.ptmm.eval;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository {

  void save(Info info);

  List<Info> all();

  Optional<Info> find(String name);

  Optional<EvaluationResult> findEvaluationResult(String name);
}
