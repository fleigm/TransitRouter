package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("eval/{id}/download")
public class DownloadController {
  private static final String[] INCLUDED_FILES = new String[]{
      Evaluation.INFO_FILE,
      EvaluationExtension.SHAPEVL_REPORT,
      Evaluation.ORIGINAL_GTFS_FEED,
      GeneratedFeedInfo.GENERATED_GTFS_FEED,
  };

  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @GET
  @Path("generated")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response downloadGeneratedFeed(@PathParam("id") UUID id) {
    return generatedFeedRepository.find(id)
        .map(info -> Response
            .ok(info.getFileStoragePath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FEED).toFile())
            .header("Content-Disposition", "attachment;filename=" + info.getName() + "." + GeneratedFeedInfo.GENERATED_GTFS_FEED)
            .build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response download(@PathParam("id") UUID id) {
    return generatedFeedRepository.find(id)
        .map(info -> Response
            .ok((StreamingOutput) output -> buildZipFile(output, info))
            .header("Content-Disposition", "attachment;filename=" + info.getName() + ".zip")
            .build())
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  private void buildZipFile(OutputStream output, GeneratedFeedInfo info) throws IOException {
    try (var archive = new ZipArchiveOutputStream(output)) {
      List<File> files = Arrays.stream(INCLUDED_FILES)
          .map(file -> info.getFileStoragePath().resolve(file).toFile())
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
