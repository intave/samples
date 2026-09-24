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
import ac.intave.samples.share.Rotation;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class PlayerFlightEventsTest {
  @Test
  void roundTripsBothInitialFlightStatesWithExistingPlayerData() throws Exception {
    UUID uuid = UUID.fromString("c577faa5-cb4b-4921-ad58-6191853bb16d");
    for (boolean flying : new boolean[]{true, false}) {
      PlayerInitEvent event = new PlayerInitEvent(
        "Player", uuid, 12, 47, 767, new Position(1, 2, 3), new Rotation(90, -30), flying
      );
      event.withOffset(5);
      String output = write(event);
      JsonObject record = JsonParser.parseString(output).getAsJsonObject();
      assertEquals("player.init", record.get("type").getAsString());
      assertEquals(flying, record.getAsJsonObject("data").get("isFlying").getAsBoolean());
      try (JsonReader reader = new JsonReader(new StringReader(output))) {
        PlayerInitEvent decoded = assertInstanceOf(PlayerInitEvent.class, reader.nextEvent());
        assertEquals(Boolean.valueOf(flying), decoded.isFlying());
        assertEquals("Player", decoded.name());
        assertEquals(uuid, decoded.uuid());
        assertEquals(12, decoded.id());
        assertEquals(47, decoded.clientVersion());
        assertEquals(767, decoded.serverVersion());
        assertEquals(new Position(1, 2, 3), decoded.position());
        assertEquals(new Rotation(90, -30), decoded.rotation());
        assertEquals(5, decoded.offset());
        assertNull(reader.nextEvent());
      }
      assertEquals(Boolean.valueOf(flying), new PlayerInitEvent(
        12, 47, 767, Position.ZERO, Rotation.ZERO, flying
      ).isFlying());
    }
  }

  @Test
  void preservesUnknownInitialFlightStateForOlderSamplesAndConstructors() throws Exception {
    String legacy = "{\"type\":\"player.init\",\"data\":{\"id\":12,\"clientVersion\":47,\"serverVersion\":47}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(legacy))) {
      PlayerInitEvent decoded = assertInstanceOf(PlayerInitEvent.class, reader.nextEvent());
      assertNull(decoded.isFlying());
      JsonObject data = JsonParser.parseString(write(decoded)).getAsJsonObject().getAsJsonObject("data");
      assertFalse(data.has("isFlying"));
    }
    assertNull(new PlayerInitEvent().isFlying());
    assertNull(new PlayerInitEvent(12, 47, 47, Position.ZERO, Rotation.ZERO).isFlying());
    assertNull(new PlayerInitEvent("Player", null, 12, 47, 47, Position.ZERO, Rotation.ZERO).isFlying());
  }

  @Test
  void roundTripsFlightToggleStatesInOrder() throws Exception {
    PlayerFlyToggleEvent enable = new PlayerFlyToggleEvent(true);
    PlayerFlyToggleEvent disable = new PlayerFlyToggleEvent(false);
    enable.withOffset(7);
    disable.withOffset(21);
    assertEquals(PlayerFlyToggleEvent.class, EventRegistry.typeNamed("player.fly_toggle").eventClass());
    String output = write(enable, disable);
    JsonObject record = JsonParser.parseString(output.split("\n")[0]).getAsJsonObject();
    assertEquals("player.fly_toggle", record.get("type").getAsString());
    assertTrue(record.getAsJsonObject("data").get("isFlying").getAsBoolean());
    try (JsonReader reader = new JsonReader(new StringReader(output))) {
      PlayerFlyToggleEvent first = assertInstanceOf(PlayerFlyToggleEvent.class, reader.nextEvent());
      PlayerFlyToggleEvent second = assertInstanceOf(PlayerFlyToggleEvent.class, reader.nextEvent());
      assertTrue(first.isFlying());
      assertFalse(second.isFlying());
      assertEquals(7, first.offset());
      assertEquals(21, second.offset());
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void dispatchesFlightToggleThroughBothTypedEntryPoints() {
    PlayerFlyToggleEvent event = new PlayerFlyToggleEvent(true);
    int[] visits = {0};
    EventSink sink = new EventSink() {
      @Override
      public void visit(PlayerFlyToggleEvent received) {
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

  private String write(Event... events) {
    StringWriter output = new StringWriter();
    try (JsonWriter writer = new JsonWriter(output)) {
      for (Event event : events) {
        event.accept(writer);
      }
    }
    return output.toString();
  }
}
