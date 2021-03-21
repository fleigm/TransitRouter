package de.fleigm.transitrouter.http.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {

  @QueryParam("page")
  @DefaultValue("1")
  @Min(value = 0, message = "Page must be positive.")
  private int page;

  @QueryParam("limit")
  @DefaultValue("30")
  @Min(value = 10, message = "Limit must be at least 10.")
  private int limit;

  public static Pagination of(int page, int limit) {
    return new Pagination(page, limit);
  }

  public int getOffset() {
    return (page - 1) * limit;
  }
}
