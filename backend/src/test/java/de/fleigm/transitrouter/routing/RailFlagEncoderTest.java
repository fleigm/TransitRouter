package de.fleigm.transitrouter.routing;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RailFlagEncoderTest {

  final RailFlagEncoder encoder = new RailFlagEncoder(new PMap());
  private final EncodingManager em = new EncodingManager.Builder()
      .add(encoder)
      .addRelationTagParser(new BusNetworkRelationTagParser())
      .build();

  private final BooleanEncodedValue accessEnc = encoder.getAccessEnc();

  @Test
  void asd() {
    ReaderWay way = new ReaderWay(1);

    way.setTag("axle_load", "22.5");
    way.setTag("electrified", "contact_line");
    way.setTag("frequency", "16.7");
    way.setTag("gauge", "1435");
    way.setTag("highspeed", "yes");
    way.setTag("incline", "0.15%");
    way.setTag("layer", "-1");
    way.setTag("lit", "yes");
    way.setTag("maxspeed", "250");
    way.setTag("meter_load", "8.0");
    way.setTag("passenger_lines", "2");
    way.setTag("rack", "no");
    way.setTag("railway", "rail");
    way.setTag("railway:ballastless", "yes");
    way.setTag("railway:bidirectional", "regular");
    way.setTag("railway:etcs", "no");
    way.setTag("railway:gnt", "no");
    way.setTag("railway:lzb", "yes");
    way.setTag("railway:preferred_direction", "forward");
    way.setTag("railway:pzb", "yes");
    way.setTag("railway:radio", "gsm-r");
    way.setTag("railway:track_class", "D4");
    way.setTag("railway:traffic_mode", "mixed");
    way.setTag("ref", "4080");
    way.setTag("ref:La", "310");
    way.setTag("start_date", "1991-06-02");
    way.setTag("tunnel", "yes");
    way.setTag("tunnel:length", "2782");
    way.setTag("tunnel:name", "Marksteintunnel");
    way.setTag("tunnel:wikidata", "Q1900931");
    way.setTag("tunnel:wikipedia", "de:Marksteintunnel");
    way.setTag("usage", "main");
    way.setTag("voltage", "15000");
    way.setTag("workrules", "DE:EBO");

    assertEquals(EncodingManager.Access.WAY, encoder.getAccess(way));
  }

}