package com.graphhopper.reader.osm;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.RAMDirectory;
import de.fleigm.ptmm.routing.BusFlagEncoder;
import de.fleigm.ptmm.routing.BusNetworkRelationTagParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class BusNetworkTest {

  @Test
  void asd() {
    EncodingManager encodingManager = EncodingManager.start()
        .add(new BusFlagEncoder())
        .addRelationTagParser(new BusNetworkRelationTagParser())
        .build();
    GraphHopperStorage ghStorage = new GraphHopperStorage(new RAMDirectory(), encodingManager, false);
    OSMReader reader = new OSMReader(ghStorage);
    ReaderRelation osmRel = new ReaderRelation(1);
    osmRel.add(new ReaderRelation.Member(ReaderRelation.WAY, 1, ""));
    osmRel.add(new ReaderRelation.Member(ReaderRelation.WAY, 2, ""));

    osmRel.setTag("route", "bus");
    reader.prepareWaysWithRelationInfo(osmRel);

    IntsRef flags = IntsRef.deepCopyOf(reader.getRelFlagsMap(1));
    assertFalse(flags.isEmpty());
  }
}
