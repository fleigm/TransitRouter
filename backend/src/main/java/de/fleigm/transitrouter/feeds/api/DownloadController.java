package de.fleigm.transitrouter.feeds.api;

import de.fleigm.transitrouter.feeds.GeneratedFeed;
import de.fleigm.transitrouter.feeds.GeneratedFeedRepository;
import de.fleigm.transitrouter.util.Helper;

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

@Path("feeds/{id}/download")
public class DownloadController {
  @Inject
  GeneratedFeedRepository generatedFeedRepository;

  @GET
  @Path("generated")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response downloadGeneratedFeed(@PathParam("id") UUID id) {
    GeneratedFeed generatedFeed = generatedFeedRepository.findOrFail(id);

    if (!generatedFeed.getStatus().finished()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    return Response
            .ok(generatedFeed.getFileStoragePath().resolve(GeneratedFeed.GENERATED_GTFS_FEED).toFile())
            .header("Content-Disposition", "attachment;filename=" + generatedFeed.getName() + "." + GeneratedFeed.GENERATED_GTFS_FEED)
            .build();
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response download(@PathParam("id") UUID id) throws IOException {
    GeneratedFeed info = generatedFeedRepository.findOrFail(id);

    List<java.nio.file.Path> files = Files.list(info.getFileStoragePath())
        .filter(path -> !Files.isDirectory(path))
        .collect(Collectors.toList());

    return Response.ok((StreamingOutput) output -> Helper.buildZipFile(output, files))
        .header("Content-Disposition", "attachment;filename=" + info.getName() + ".zip")
        .build();
  }


}
