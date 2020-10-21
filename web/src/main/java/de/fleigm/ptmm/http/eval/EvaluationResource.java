package de.fleigm.ptmm.http.eval;

import de.fleigm.ptmm.eval.Report;
import de.fleigm.ptmm.eval.ReportEntry;
import de.fleigm.ptmm.http.pagination.Page;
import de.fleigm.ptmm.http.pagination.Paged;
import de.fleigm.ptmm.http.sort.SortQuery;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Path("eval/{name}")
public class EvaluationResource {

  @Inject
  Reports reports;

  @PathParam("name")
  String name;


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(
      @Context UriInfo uriInfo,
      @BeanParam Paged paged,
      @QueryParam("search") @DefaultValue("") String search,
      @QueryParam("sort") @DefaultValue("") String sort) {

    Report report = reports.get(name);

    List<ReportEntry> entriesMatchingSearch = report.entries();
    if (!search.isBlank()) {
      entriesMatchingSearch = report.entries()
          .stream()
          .filter(reportEntry -> reportEntry.tripId.contains(search))
          .collect(Collectors.toList());
    }

    if (!sort.isBlank()) {
      Comparator<ReportEntry> comparator = createComparator(SortQuery.parse(sort));
      if (comparator != null) {
        if (SortQuery.parse(sort).order() == SortQuery.SortOrder.DESC) {
          comparator = comparator.reversed();
        }
        entriesMatchingSearch.sort(comparator);
      }
    }

    List<ReportEntry> entries = entriesMatchingSearch.stream()
        .skip(paged.getOffset())
        .limit(paged.getLimit())
        .collect(Collectors.toList());

    var page = Page.builder()
        .setData(entries)
        .setCurrentPage(paged.getPage())
        .setPerPage(paged.getLimit())
        .setTotal(entriesMatchingSearch.size())
        .setUri(uriInfo.getAbsolutePath())
        .create();

    return Response.ok(page).build();
  }

  private Comparator<ReportEntry> createComparator(SortQuery sortQuery) {
    Comparator<ReportEntry> comparator = null;
    switch (sortQuery.attribute()) {
      case "an":
        comparator = Comparator.comparingDouble(ReportEntry::an);
        break;
      case "al":
        comparator = Comparator.comparingDouble(ReportEntry::al);
        break;
      case "avgFd":
        comparator = Comparator.comparingDouble(ReportEntry::avgFd);
        break;
    }
    return comparator;
  }
}
