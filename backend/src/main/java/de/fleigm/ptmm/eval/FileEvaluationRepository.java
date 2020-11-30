package de.fleigm.ptmm.eval;

import de.fleigm.ptmm.TransitFeed;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class FileEvaluationRepository implements EvaluationRepository {
  private List<Info> evaluations = new ArrayList<>();

  @ConfigProperty(name = "evaluation.folder")
  String evaluationBasePath;

  @PostConstruct
  public void init() {
    try {
      evaluations = loadFromDisk(evaluationBasePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void save(Info info) {
    if (!evaluations.contains(info)) {
      evaluations.add(info);
    }

    try {
      writeToDisk(info);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<Exception> delete(String name) {
    Optional<Info> info = find(name);

    if (info.isEmpty()) {
      return Optional.empty();
    }
    // deleting an evaluation that is still in progress would cause many errors.
    if (info.get().getStatus() == Status.PENDING) {
      return Optional.of(new IllegalStateException(String.format("Cannot delete pending evaluation %s.", name)));
    }

    evaluations.remove(info.get());

    try {
      FileUtils.deleteDirectory(Paths.get(evaluationBasePath, name).toFile());
    } catch (IOException e) {
      log.error("Could not delete evaluation folder with name {}", name, e);
      return Optional.of(new RuntimeException(String.format("Could not delete evaluation folder with name %s", name), e));
    }

    return Optional.empty();
  }

  @Override
  public List<Info> all() {
    return evaluations;
  }

  @Override
  public Optional<Info> find(String name) {
    return evaluations.stream()
        .filter(info -> info.getName().equalsIgnoreCase(name))
        .findFirst();
  }

  @Override
  @CacheResult(cacheName = "evaluation-result-cache")
  public Optional<EvaluationResult> findEvaluationResult(String name) {
    if (find(name).isEmpty()) {
      return Optional.empty();
    }

    CompletableFuture<TransitFeed> originalTransitFeedSupplier = CompletableFuture.supplyAsync(() ->
        new TransitFeed(evaluationBasePath + name + "/gtfs.original.zip"));

    CompletableFuture<TransitFeed> generatedTransitFeedSupplier = CompletableFuture.supplyAsync(() ->
        new TransitFeed(evaluationBasePath + name + "/gtfs.generated.zip"));

    CompletableFuture<Report> reportSupplier = CompletableFuture.supplyAsync(() ->
        Report.read(evaluationBasePath + name + "/gtfs.generated.fullreport.tsv"));

    try {
      return Optional.of(new EvaluationResult(
          reportSupplier.get(),
          originalTransitFeedSupplier.get(),
          generatedTransitFeedSupplier.get()
      ));
    } catch (Exception e) {
      throw new RuntimeException(String.format("Could not load evaluation result %s", name), e);
    }
  }

  void writeToDisk(Info info) throws IOException {
    Jsonb json = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    Files.createDirectories(Paths.get(evaluationBasePath, info.getName()));
    Files.writeString(Paths.get(evaluationBasePath, info.getName(), "info.json"), json.toJson(info));
  }

  List<Info> loadFromDisk(String baseFolder) throws IOException {
    Jsonb json = JsonbBuilder.create();

    Path root = Path.of(baseFolder);
    return Files.walk(root, 2, new FileVisitOption[0])
        .filter(path -> path.endsWith("info.json"))
        .map(path -> {
          try {
            return Files.readString(path);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .map(rawJson -> json.fromJson(rawJson, Info.class))
        .peek(info -> info.setPath(root.resolve(info.getName())))
        .collect(Collectors.toList());

  }

  @CacheInvalidateAll(cacheName = "evaluation-result-cache")
  public void invalidateCache() {
    init();
  }
}
