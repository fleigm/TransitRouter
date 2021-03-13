package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.graphhopper.util.shapes.GHPoint;
import de.fleigm.transitrouter.routing.Observation;

import java.io.IOException;

public class ObservationDeserializer extends StdDeserializer<Observation> {

  public ObservationDeserializer() {
    super(Observation.class);
  }

  @Override
  public Observation deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    TreeNode treeNode = parser.readValueAsTree();
    GHPoint point = new GHPoint(
        ((DoubleNode) treeNode.get(0)).doubleValue(),
        ((DoubleNode) treeNode.get(1)).doubleValue()
    );

    return new Observation(point);
  }
}
