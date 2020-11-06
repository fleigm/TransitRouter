package de.fleigm.ptmm;

import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

;

class FrechetDistanceTest {
  private static final double TEST_DELTA = 0.0001;

  @Test
  void some_test() throws IOException {
    TransitFeed original = new TransitFeed("../../files/stuttgart.zip");
    TransitFeed generated = new TransitFeed("../../files/stuttgart_generated.zip");

    List<Entry> entries = Files.lines(Paths.get("../../eval/stuttgart_generated.fullreport.tsv"))
        .map(s -> s.split("\t"))
        .map(strings -> new Entry(strings[0], Double.parseDouble(strings[3])))
        .limit(1)
        .collect(Collectors.toList());

    List<PointList> originalShapes = entries.stream()
        .map(entry -> original.trips().get(entry.trip))
        .map(trip -> original.internal().getShape(trip.shape_id).geometry)
        .map(this::convert)
        .collect(Collectors.toList());

    List<PointList> generatedShapes = entries.stream()
        .map(entry -> generated.trips().get(entry.trip))
        .map(trip -> generated.internal().getShape(trip.shape_id).geometry)
        .map(this::convert)
        .collect(Collectors.toList());

    for (int i = 0; i < entries.size(); i++) {
      assertEquals(entries.get(i).df, FrechetDistance.compute(originalShapes.get(i), generatedShapes.get(i)), TEST_DELTA);
    }
  }

  @Test
  void asd() throws IOException {
    List<Double> values = Files.lines(Paths.get("../../eval/stuttgart_generated.fullreport.tsv"))
        .map(s -> s.split("\t"))
        .map(strings -> Double.parseDouble(strings[1]))
        .collect(Collectors.toList());

    double acc0 = 0;
    double acc10 = 0;
    double acc20 = 0;
    double acc40 = 0;
    double acc80 = 0;
    for (Double value : values) {
      if (value <= 0.00001) {
        acc0++;
      }
      if (value <= 0.1) {
        acc10++;
      }
      if (value <= 0.2) {
        acc20++;
      }
      if (value <= 0.4) {
        acc40++;
      }
      if (value <= 0.8) {
        acc80++;
      }
    }

    acc0 /= values.size();
    acc10 /= values.size();
    acc20 /= values.size();
    acc40 /= values.size();
    acc80 /= values.size();

    assertNotNull(values);
  }

  private PointList convert(LineString lineString) {
    CoordinateSequence coordinateSequence = lineString.getCoordinateSequence();
    PointList pointList = new PointList(coordinateSequence.size(), false);
    for (int i = 0; i < coordinateSequence.size(); i++) {
      pointList.add(coordinateSequence.getY(i), coordinateSequence.getX(i));
    }
    return pointList;
  }

  private static class Entry {
    String trip;
    double df;

    public Entry(String trip, double df) {
      this.trip = trip;
      this.df = df;
    }
  }


}