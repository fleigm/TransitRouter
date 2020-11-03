package de.fleigm.ptmm.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Info {

  @EqualsAndHashCode.Include
  private String name;
  private Parameters parameters;
  private LocalDateTime createdAt;
  private String path;
  private Status status;
}
