package de.fleigm.ptmm.eval;

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
import java.util.stream.Collectors;

@ApplicationScoped
public class FileEvaluationRepository implements EvaluationRepository {
  @ConfigProperty(name = "evaluation.folder")
  String evaluationBasePath;
  private List<Info> evaluations = new ArrayList<>();

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
  public EvaluationResult findEvaluationResult(String name) {
    return null;
  }

  void writeToDisk(Info info) throws IOException {
    Jsonb json = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    Files.writeString(Paths.get(evaluationBasePath, info.getName(), "info.json"), json.toJson(info));
  }

  List<Info> loadFromDisk(String baseFolder) throws IOException {
    Jsonb json = JsonbBuilder.create();

    return Files.walk(Path.of(baseFolder), 2, new FileVisitOption[0])
        .filter(path -> path.endsWith("info.json"))
        .map(path -> {
          try {
            return Files.readString(path);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .map(rawJson -> json.fromJson(rawJson, Info.class))
        .collect(Collectors.toList());

  }
}
