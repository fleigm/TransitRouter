package de.fleigm.ptmm.eval;

import java.util.Objects;

public class ReportEntry {
  public final String tripId;
  public final double an;
  public final double al;
  public final double avgFd;

  public ReportEntry(String tripId, double an, double al, double avgFd) {
    this.tripId = tripId;
    this.an = an;
    this.al = al;
    this.avgFd = avgFd;
  }

  public String tripId() {
    return tripId;
  }

  public double an() {
    return an;
  }

  public double al() {
    return al;
  }

  public double avgFd() {
    return avgFd;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ReportEntry)) return false;
    ReportEntry that = (ReportEntry) o;
    return Double.compare(that.an, an) == 0 &&
           Double.compare(that.al, al) == 0 &&
           Double.compare(that.avgFd, avgFd) == 0 &&
           Objects.equals(tripId, that.tripId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tripId, an, al, avgFd);
  }

  @Override
  public String toString() {
    return String.format("ReportEntry{tripId='%s', an=%s, al=%s, avgFd=%s}", tripId, an, al, avgFd);
  }
}
