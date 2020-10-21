package de.fleigm.ptmm.http.pagination;

import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

public class Paged {

  @QueryParam("offset")
  @DefaultValue("0")
  @Min(value = 0, message = "Offset must be positive.")
  private int offset;

  @QueryParam("limit")
  @DefaultValue("30")
  @Min(value = 10, message = "Limit must be at least 10.")
  private int limit;

  @QueryParam("page")
  @DefaultValue("1")
  @Min(value = 0, message = "Page must be positive.")
  private int page;

  public int getOffset() {
    return offset != 0 ? offset : (page - 1) * limit;
  }

  public int getLimit() {
    return limit;
  }

  public int getPage() {
    return page;
  }
}
