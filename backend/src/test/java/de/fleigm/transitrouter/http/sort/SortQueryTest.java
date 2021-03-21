package de.fleigm.transitrouter.http.sort;

import de.fleigm.transitrouter.http.sort.SortQuery.SortOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortQueryTest {

  @Test
  void parse() {
      assertEquals(SortQuery.of("a", SortOrder.ASC), SortQuery.parse("a:asc"));
      assertEquals(SortQuery.of("a", SortOrder.DESC), SortQuery.parse("a:desc"));

      assertThrows(IllegalArgumentException.class, () -> SortQuery.parse(""));
      assertThrows(IllegalArgumentException.class, () -> SortQuery.parse("a"));
      assertThrows(IllegalArgumentException.class, () -> SortQuery.parse(":desc"));
  }

}