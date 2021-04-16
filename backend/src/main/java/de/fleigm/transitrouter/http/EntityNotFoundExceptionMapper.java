package de.fleigm.transitrouter.http;

import de.fleigm.transitrouter.data.EntityNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps a {@link EntityNotFoundException} to a 404 not found HTTP response.
 */
@Provider
@ApplicationScoped
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

  @Override
  public Response toResponse(EntityNotFoundException exception) {
    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
