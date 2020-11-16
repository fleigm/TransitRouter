package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.EvaluationRepository;
import de.fleigm.ptmm.eval.Info;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("eval/{name}/download")
public class DownloadController {
  private static final String[] INCLUDED_FILES = new String[]{
      Evaluation.INFO_FILE,
      Evaluation.GTFS_FULL_REPORT,
      Evaluation.ORIGINAL_GTFS_FEED,
      Evaluation.GENERATED_GTFS_FEED,
      Evaluation.SHAPEVL_OUTPUT
  };

  @Inject
  EvaluationRepository evaluationRepository;

  @ConfigProperty(name = "evaluation.folder")
  String evaluationFolder;

  @GET
  @Path("generated")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response downloadGeneratedFeed(@PathParam("name") String name) {
    return evaluationRepository.find(name)
        .map(info -> Response
            .ok(Paths.get(evaluationFolder, name, Evaluation.GENERATED_GTFS_FEED))
            .header("Content-Disposition", "attachment;filename=" + info.getName() + "." + Evaluation.GENERATED_GTFS_FEED)
            .build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response download(@PathParam("name") String name) {
    return evaluationRepository.find(name)
        .map(info -> Response
            .ok((StreamingOutput) output -> buildZipFile(output, info))
            .header("Content-Disposition", "attachment;filename=" + info.getName() + ".zip")
            .build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  private void buildZipFile(OutputStream output, Info info) throws IOException {
    try (var archive = new ZipArchiveOutputStream(output)) {
      List<File> files = Arrays.stream(INCLUDED_FILES)
          .map(f -> Paths.get(evaluationFolder, info.getName(), f).toFile())
          .collect(Collectors.toList());

      for (var file : files) {
        ArchiveEntry entry = new ZipArchiveEntry(file, file.getName());
        archive.putArchiveEntry(entry);
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
          IOUtils.copy(inputStream, archive);
        }
        archive.closeArchiveEntry();
      }
    }
  }

}
