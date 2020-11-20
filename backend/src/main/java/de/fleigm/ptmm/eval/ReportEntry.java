package de.fleigm.ptmm.eval;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class ReportEntry {
  public final String tripId;
  public final double an;
  public final double al;
  public final double avgFd;
}
