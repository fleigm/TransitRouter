package de.fleigm.ptmm.eval.api;

import de.fleigm.ptmm.eval.Evaluation;
import de.fleigm.ptmm.eval.EvaluationExtension;
import de.fleigm.ptmm.eval.GeneratedFeedInfo;
import de.fleigm.ptmm.eval.GeneratedFeedRepository;
import de.fleigm.ptmm.util.Helper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.nio.file.Files;
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
    GeneratedFeedInfo info = generatedFeedRepository.findOrFail(id);

    return Response
            .ok(info.getFileStoragePath().resolve(GeneratedFeedInfo.GENERATED_GTFS_FEED).toFile())
            .header("Content-Disposition", "attachment;filename=" + info.getName() + "." + GeneratedFeedInfo.GENERATED_GTFS_FEED)
            .build();
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response download(@PathParam("id") UUID id) throws IOException {
    GeneratedFeedInfo info = generatedFeedRepository.findOrFail(id);

    List<java.nio.file.Path> files = Files.list(info.getFileStoragePath())
        .filter(path -> !Files.isDirectory(path))
        .collect(Collectors.toList());

    return Response.ok((StreamingOutput) output -> Helper.buildZipFile(output, files))
        .header("Content-Disposition", "attachment;filename=" + info.getName() + ".zip")
        .build();
  }


}
