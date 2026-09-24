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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

final class PlayerVitalsEventTest {
  @Test
  void roundTripsCombinedSnapshotThroughRegisteredWireFormat() throws Exception {
    PlayerVitalsEvent event = new PlayerVitalsEvent(
      17.5f, 16, 3.25f, 0.75f, 12, 250,
      40.5, 6.25, 1.75f, 18.5, 8.0
    );
    event.withOffset(42);
    StringWriter output = new StringWriter();
    try (JsonWriter writer = new JsonWriter(output)) {
      event.accept(writer);
    }

    JsonObject record = JsonParser.parseString(output.toString()).getAsJsonObject();
    assertEquals("player.vitals", record.get("type").getAsString());
    JsonObject data = record.getAsJsonObject("data");
    assertEquals(17.5f, data.get("health").getAsFloat());
    assertEquals(16, data.get("foodLevel").getAsInt());
    assertEquals(3.25f, data.get("saturation").getAsFloat());
    assertEquals(0.75f, data.get("xpProgress").getAsFloat());
    assertEquals(12, data.get("xpLevel").getAsInt());
    assertEquals(250, data.get("totalXp").getAsInt());
    assertEquals(40.5, data.get("maxHealth").getAsDouble());
    assertEquals(6.25, data.get("absorption").getAsDouble());
    assertEquals(1.75f, data.get("exhaustion").getAsFloat());
    assertEquals(18.5, data.get("armor").getAsDouble());
    assertEquals(8.0, data.get("armorToughness").getAsDouble());

    try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
      PlayerVitalsEvent decoded = assertInstanceOf(PlayerVitalsEvent.class, reader.nextEvent());
      assertEquals(17.5f, decoded.health());
      assertEquals(16, decoded.foodLevel());
      assertEquals(3.25f, decoded.saturation());
      assertEquals(0.75f, decoded.xpProgress());
      assertEquals(12, decoded.xpLevel());
      assertEquals(250, decoded.totalXp());
      assertEquals(Double.valueOf(40.5), decoded.maxHealth());
      assertEquals(Double.valueOf(6.25), decoded.absorption());
      assertEquals(Float.valueOf(1.75f), decoded.exhaustion());
      assertEquals(Double.valueOf(18.5), decoded.armor());
      assertEquals(Double.valueOf(8.0), decoded.armorToughness());
      assertEquals(42, decoded.offset());
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void readsOlderSnapshotsWithoutInventingAttributeValues() throws Exception {
    String legacyRecord = "{\"type\":\"player.vitals\",\"data\":{\"health\":17.5,\"foodLevel\":16,"
      + "\"saturation\":3.25,\"xpProgress\":0.75,\"xpLevel\":12,\"totalXp\":250}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(legacyRecord))) {
      PlayerVitalsEvent decoded = assertInstanceOf(PlayerVitalsEvent.class, reader.nextEvent());
      assertEquals(17.5f, decoded.health());
      assertEquals(250, decoded.totalXp());
      assertUnknownAttributes(decoded);
    }

    PlayerVitalsEvent legacy = new PlayerVitalsEvent(17.5f, 16, 3.25f, 0.75f, 12, 250);
    assertUnknownAttributes(legacy);
    StringWriter output = new StringWriter();
    try (JsonWriter writer = new JsonWriter(output)) {
      legacy.accept(writer);
    }
    JsonObject data = JsonParser.parseString(output.toString()).getAsJsonObject().getAsJsonObject("data");
    assertFalse(data.has("maxHealth"));
    assertFalse(data.has("absorption"));
    assertFalse(data.has("exhaustion"));
    assertFalse(data.has("armor"));
    assertFalse(data.has("armorToughness"));
  }

  @Test
  void roundTripsKnownZeroAttributesSeparatelyFromUnknownValues() throws Exception {
    PlayerVitalsEvent event = new PlayerVitalsEvent(
      20, 20, 5, 0, 0, 0, 20.0, 0.0, 0.0f, 0.0, 0.0
    );
    StringWriter output = new StringWriter();
    try (JsonWriter writer = new JsonWriter(output)) {
      event.accept(writer);
    }
    try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
      PlayerVitalsEvent decoded = assertInstanceOf(PlayerVitalsEvent.class, reader.nextEvent());
      assertEquals(Double.valueOf(20.0), decoded.maxHealth());
      assertEquals(Double.valueOf(0.0), decoded.absorption());
      assertEquals(Float.valueOf(0.0f), decoded.exhaustion());
      assertEquals(Double.valueOf(0.0), decoded.armor());
      assertEquals(Double.valueOf(0.0), decoded.armorToughness());
    }
  }

  private void assertUnknownAttributes(PlayerVitalsEvent event) {
    assertNull(event.maxHealth());
    assertNull(event.absorption());
    assertNull(event.exhaustion());
    assertNull(event.armor());
    assertNull(event.armorToughness());
  }

  @Test
  void dispatchesThroughBothEntryPointsToTypedVisitor() {
    PlayerVitalsEvent event = new PlayerVitalsEvent(0, 0, 0, 0, 0, 0);
    int[] visits = {0};
    EventSink sink = new EventSink() {
      @Override
      public void visit(PlayerVitalsEvent received) {
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
