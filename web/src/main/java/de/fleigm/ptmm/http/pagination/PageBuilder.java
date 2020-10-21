package de.fleigm.ptmm.http.pagination;

import java.net.URI;
import java.util.List;

public class PageBuilder {
  private URI uri;
  private int currentPage;
  private int perPage;
  private long total;
  private List data;

  public PageBuilder setUri(URI uri) {
    this.uri = uri;
    return this;
  }

  public PageBuilder setCurrentPage(int currentPage) {
    this.currentPage = currentPage;
    return this;
  }

  public PageBuilder setPerPage(int perPage) {
    this.perPage = perPage;
    return this;
  }

  public PageBuilder setTotal(long total) {
    this.total = total;
    return this;
  }

  public PageBuilder setData(List data) {
    this.data = data;
    return this;
  }

  public Page create() {
    return new Page(uri, currentPage, perPage, total, data);
  }
}