package de.fleigm.transitrouter.http.pagination;

import lombok.Data;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;


@Data
public class Page<T> {

  private final long total;
  private final int perPage;
  private final long from;
  private final long to;
  private final int currentPage;
  private final int lastPage;
  private URI prevPageUrl;
  private URI nextPageUrl;
  private URI firstPageUrl;
  private URI lastPageUrl;

  private final List<T> data;

  public Page(URI uri, int currentPage, int perPage, long total, List<T> data) {
    this.currentPage = currentPage;
    this.perPage = perPage;
    this.total = total;
    this.data = data;
    this.lastPage = (int) Math.ceil(total / (double) perPage);
    this.from = (currentPage - 1) * perPage;
    this.to = currentPage * perPage;
    createUrls(uri);
  }

  public static <T> PageBuilder<T> builder() {
    return new PageBuilder<T>();
  }


  private void createUrls(URI uri) {
    lastPageUrl = UriBuilder.fromUri(uri)
        .queryParam("page", lastPage)
        .build();

    firstPageUrl = UriBuilder.fromUri(uri)
        .queryParam("page", 1)
        .build();

    if (currentPage != lastPage) {
      nextPageUrl = UriBuilder.fromUri(uri)
          .queryParam("page", currentPage + 1)
          .build();
    }

    if (currentPage != 1) {
      prevPageUrl = UriBuilder.fromUri(uri)
          .queryParam("page", currentPage - 1)
          .build();
    }
  }

  public static class PageBuilder<T> {
    private URI uri;
    private int currentPage;
    private int perPage;
    private long total;
    private List<T> data;

    PageBuilder() {
    }

    public PageBuilder<T> uri(URI uri) {
      this.uri = uri;
      return this;
    }

    public PageBuilder<T> currentPage(int currentPage) {
      this.currentPage = currentPage;
      return this;
    }

    public PageBuilder<T> perPage(int perPage) {
      this.perPage = perPage;
      return this;
    }

    public PageBuilder<T> total(long total) {
      this.total = total;
      return this;
    }

    public PageBuilder<T> data(List<T> data) {
      this.data = data;
      return this;
    }

    public Page<T> build() {
      return new Page<T>(uri, currentPage, perPage, total, data);
    }

    public String toString() {
      return "Page.PageBuilder(uri=" + this.uri + ", currentPage=" + this.currentPage + ", perPage=" + this.perPage + ", total=" + this.total + ", data=" + this.data + ")";
    }
  }
}
