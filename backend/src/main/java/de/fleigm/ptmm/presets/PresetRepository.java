package de.fleigm.ptmm.presets;

import de.fleigm.ptmm.data.DataRoot;
import de.fleigm.ptmm.data.Repository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class PresetRepository extends Repository<Preset> {



  public static class Producer {
    @Produces
    @ApplicationScoped
    public PresetRepository get(DataRoot dataRoot) {
      return dataRoot.presets();
    }
  }
}
