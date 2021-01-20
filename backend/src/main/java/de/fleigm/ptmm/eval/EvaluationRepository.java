package de.fleigm.ptmm.eval;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class EvaluationRepository {
  @ConfigProperty(name = "evaluation.folder")
  String evaluationBasePath;
  private List<GeneratedFeedInfo> evaluations = new ArrayList<>();

  @PostConstruct
  public void init() {
    try {
      evaluations = loadFromDisk(evaluationBasePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void save(GeneratedFeedInfo info) {
    info.setBasePath(Path.of(evaluationBasePath));

    if (!evaluations.contains(info)) {
      evaluations.add(info);
    }

    try {
      writeToDisk(info);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<Exception> delete(UUID id) {
    Optional<GeneratedFeedInfo> info = find(id);

    if (info.isEmpty()) {
      return Optional.empty();
    }
    // deleting an evaluation that is still in progress would cause many errors.
    if (info.get().getStatus() == Status.PENDING) {
      return Optional.of(new IllegalStateException(String.format("Cannot delete pending evaluation %s.", id)));
    }

    evaluations.remove(info.get());

    try {
      FileUtils.deleteDirectory(info.get().getPath().toFile());
    } catch (IOException e) {
      log.error("Could not delete evaluation folder with name {}", id, e);
      return Optional.of(new RuntimeException(String.format("Could not delete evaluation folder with name %s", id), e));
    }

    return Optional.empty();
  }

  public List<GeneratedFeedInfo> all() {
    return evaluations;
  }

  public Optional<GeneratedFeedInfo> find(UUID id) {
    return evaluations.stream()
        .filter(info -> info.getId().equals(id))
        .findFirst();
  }

  @CacheResult(cacheName = "evaluation-result-cache")
  public Optional<EvaluationResult> findEvaluationResult(UUID id) {
    return find(id).filter(GeneratedFeedInfo::hasFinished).map(EvaluationResult::load);
  }

  void writeToDisk(GeneratedFeedInfo info) throws IOException {
    Jsonb json = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    Files.createDirectories(info.getPath());
    Files.writeString(info.getPath().resolve(Evaluation.INFO_FILE), json.toJson(info));
  }

  List<GeneratedFeedInfo> loadFromDisk(String baseFolder) throws IOException {
    Jsonb json = JsonbBuilder.create();

    Path root = Path.of(baseFolder);
    return Files.walk(root, 2, new FileVisitOption[0])
        .filter(path -> path.endsWith(Evaluation.INFO_FILE))
        .map(path -> {
          try {
            return Files.readString(path);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .map(rawJson -> json.fromJson(rawJson, GeneratedFeedInfo.class))
        .peek(info -> info.setBasePath(root))
        .collect(Collectors.toList());

  }

  @CacheInvalidateAll(cacheName = "evaluation-result-cache")
  public void invalidateCache() {
    init();
  }
}
