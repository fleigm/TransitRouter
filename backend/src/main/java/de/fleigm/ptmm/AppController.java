package de.fleigm.ptmm;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("app")
public class AppController {

  @Inject
  App app;

  @POST
  public Response shutdownStorage() {
    app.shutdown();

    return Response.ok().build();
  }
}
