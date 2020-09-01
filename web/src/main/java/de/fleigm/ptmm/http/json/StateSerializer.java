package de.fleigm.ptmm.http.json;

import com.graphhopper.matching.State;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class StateSerializer implements JsonbSerializer<State> {

  @Override
  public void serialize(State state, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartObject();

    generator.write("node", state.getQueryResult().getClosestNode());

    generator.writeStartArray("position")
        .write(state.getQueryResult().getSnappedPoint().lat)
        .write(state.getQueryResult().getSnappedPoint().lon)
        .writeEnd();

    if (state.isOnDirectedEdge() && state.getIncomingVirtualEdge() != null) {
      generator.writeStartArray("incomingEdge")
          .write(state.getIncomingVirtualEdge().getBaseNode())
          .write(state.getIncomingVirtualEdge().getAdjNode())
          .writeEnd();
    }

    if (state.isOnDirectedEdge() && state.getOutgoingVirtualEdge() != null) {
      generator.writeStartArray("outgoingEdge")
          .write(state.getOutgoingVirtualEdge().getBaseNode())
          .write(state.getOutgoingVirtualEdge().getAdjNode())
          .writeEnd();
    }

    generator.writeEnd();
  }
}
