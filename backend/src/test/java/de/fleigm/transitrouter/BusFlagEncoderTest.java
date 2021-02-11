package de.fleigm.transitrouter;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.Roundabout;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.PMap;
import de.fleigm.transitrouter.routing.BusFlagEncoder;
import de.fleigm.transitrouter.routing.BusNetworkRelationTagParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BusFlagEncoderTest {

  final BusFlagEncoder encoder = createEncoder();
  private final EncodingManager em = new EncodingManager.Builder()
      .add(encoder)
      .addRelationTagParser(new BusNetworkRelationTagParser())
      .build();

  private final BooleanEncodedValue roundaboutEnc = em.getBooleanEncodedValue(Roundabout.KEY);
  private final DecimalEncodedValue avSpeedEnc = encoder.getAverageSpeedEnc();
  private final BooleanEncodedValue accessEnc = encoder.getAccessEnc();

  BusFlagEncoder createEncoder() {
    return new BusFlagEncoder(new PMap("speed_two_directions=true|block_fords=true"));
  }

  @Test
  void can_use_bus_lanes() {
    ReaderWay way = new ReaderWay(1);
    way.setTag("bus", "yes");
    way.setTag("highway", "service");
    way.setTag("vehicle", "no");

    assertEquals(EncodingManager.Access.WAY, encoder.getAccess(way));
  }

  @Test
  void allow_max_speed_below_5() {
    ReaderWay way = new ReaderWay(1);
    way.setTag("maxspeed", "2");
    way.setTag("vehicle", "yes");
    way.setTag("highway", "secondary");

    EncodingManager.AcceptWay allowed = new EncodingManager.AcceptWay();
    for (FlagEncoder encoder : em.fetchEdgeEncoders())
      allowed.put(encoder.toString(), EncodingManager.Access.WAY);
    IntsRef relFlags = em.createRelationFlags();
    IntsRef edgeFlags = em.handleWayTags(way, allowed, relFlags);

    assertEquals(2, avSpeedEnc.getDecimal(false, edgeFlags), 1e-1);
  }

  @Test
  void allow_oneway_with_bus_lane() {
    ReaderWay way = new ReaderWay(1);
    way.setTag("highway", "secondary");
    way.setTag("busway", "opposite_lane");
    way.setTag("oneway", "yes");

    EncodingManager.AcceptWay allowed = new EncodingManager.AcceptWay();
    for (FlagEncoder encoder : em.fetchEdgeEncoders())
      allowed.put(encoder.toString(), EncodingManager.Access.WAY);
    IntsRef relFlags = em.createRelationFlags();
    IntsRef edgeFlags = em.handleWayTags(way, allowed, relFlags);

    assertTrue(accessEnc.getBool(false, edgeFlags));
    assertTrue(accessEnc.getBool(true, edgeFlags));
  }
}
