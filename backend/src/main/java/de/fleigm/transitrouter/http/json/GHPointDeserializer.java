package de.fleigm.transitrouter.http.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.graphhopper.util.shapes.GHPoint;

import java.io.IOException;

public class GHPointDeserializer extends StdDeserializer<GHPoint> {

  public GHPointDeserializer() {
    super(GHPoint.class);
  }

  @Override
  public GHPoint deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
    TreeNode treeNode = parser.readValueAsTree();
    return new GHPoint(
        ((DoubleNode) treeNode.get(0)).doubleValue(),
        ((DoubleNode) treeNode.get(1)).doubleValue()
    );
  }

}
