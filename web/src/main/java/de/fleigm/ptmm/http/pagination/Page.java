package de.fleigm.ptmm.http.pagination;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;


public class Page<T> {

  private long total;
  private int perPage;
  private long from;
  private long to;
  private int currentPage;
  private int lastPage;
  private URI prevPageUrl;
  private URI nextPageUrl;
  private URI firstPageUrl;
  private URI lastPageUrl;

  private List<T> data;

  Page(URI uri, int currentPage, int perPage, long total, List<T> data) {
    this.currentPage = currentPage;
    this.perPage = perPage;
    this.total = total;
    this.data = data;
    this.lastPage = (int) Math.ceil(total / (double) perPage);
    this.from = (currentPage - 1) * perPage;
    this.to = currentPage * perPage;
    createUrls(uri);
  }

  public static PageBuilder builder() {
    return new PageBuilder();
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

  public long getTotal() {
    return total;
  }

  public int getPerPage() {
    return perPage;
  }

  public long getFrom() {
    return from;
  }

  public long getTo() {
    return to;
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public URI getPrevPageUrl() {
    return prevPageUrl;
  }

  public URI getNextPageUrl() {
    return nextPageUrl;
  }

  public URI getFirstPageUrl() {
    return firstPageUrl;
  }

  public URI getLastPageUrl() {
    return lastPageUrl;
  }

  public List<T> getData() {
    return data;
  }
}
