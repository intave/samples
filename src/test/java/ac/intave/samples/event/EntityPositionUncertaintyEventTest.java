/*
 * Copyright 2026 Intave
 *
 * This software is licensed under the PolyForm Perimeter License 1.0.0.
 * You may use this software for any purpose, except for providing to
 * others any product that competes with the software.
 *
 * A copy of the license is available at:
 *   https://polyformproject.org/licenses/perimeter/1.0.0/
 */

package ac.intave.samples.event;

import ac.intave.samples.serial.JsonReader;
import ac.intave.samples.serial.JsonWriter;
import ac.intave.samples.share.Position;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

final class EntityPositionUncertaintyEventTest {
  @Test
  void roundTripsUncertainAndExactPositions() throws Exception {
    Position min = new Position(-12.75, 64, 3.125);
    for (Position max : Arrays.asList(new Position(-12.25, 64.5, 3.875), min)) {
      EntityPositionUncertaintyEvent event = new EntityPositionUncertaintyEvent(42, min, max);
      event.withOffset(17);
      StringWriter output = new StringWriter();
      try (JsonWriter writer = new JsonWriter(output)) {
        event.accept(writer);
      }
      JsonObject record = JsonParser.parseString(output.toString()).getAsJsonObject();
      assertEquals("entity.position_uncertainty", record.get("type").getAsString());
      JsonObject data = record.getAsJsonObject("data");
      assertEquals(42, data.get("entityId").getAsInt());
      assertEquals(-12.75, data.getAsJsonObject("minPosition").get("x").getAsDouble());
      assertEquals(max.z(), data.getAsJsonObject("maxPosition").get("z").getAsDouble());

      try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
        EntityPositionUncertaintyEvent decoded = assertInstanceOf(
          EntityPositionUncertaintyEvent.class, reader.nextEvent()
        );
        assertEquals(42, decoded.entityId());
        assertEquals(min, decoded.minPosition());
        assertEquals(max, decoded.maxPosition());
        assertEquals(17, decoded.offset());
        assertNull(reader.nextEvent());
      }
    }
  }

  @Test
  void dispatchesThroughBothTypedEntryPoints() {
    EntityPositionUncertaintyEvent event = new EntityPositionUncertaintyEvent(42, Position.ZERO, Position.ZERO);
    int[] visits = {0};
    EventSink sink = new EventSink() {
      @Override
      public void visit(EntityPositionUncertaintyEvent received) {
        assertSame(event, received);
        visits[0]++;
      }

      @Override
      public void visitAny(Event received) {
        fail("Typed visitor was bypassed");
      }

      @Override
      public String name() {
        return "test";
      }
    };
    event.accept(sink);
    sink.visitSelect(event);
    assertEquals(2, visits[0]);
  }
}
