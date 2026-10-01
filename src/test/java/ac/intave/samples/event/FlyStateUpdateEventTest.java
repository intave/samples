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

final class FlyStateUpdateEventTest {
  @Test
  void roundTripsFlightContextAndDisagreeingStatesThroughRegisteredWireFormat() throws Exception {
    for (String gameMode : new String[]{"SURVIVAL", "CREATIVE", "ADVENTURE", "SPECTATOR", "FUTURE_MODE"}) {
      FlyStateUpdateEvent event = new FlyStateUpdateEvent(true, 0.075f, gameMode, true, false);
      event.withOffset(42);
      String output = write(event);
      JsonObject record = JsonParser.parseString(output).getAsJsonObject();
      assertEquals("player.fly_state_update", record.get("type").getAsString());
      JsonObject data = record.getAsJsonObject("data");
      assertTrue(data.get("allowFlight").getAsBoolean());
      assertEquals(0.075f, data.get("flySpeed").getAsFloat());
      assertEquals(gameMode, data.get("gameMode").getAsString());
      assertTrue(data.get("clientFlying").getAsBoolean());
      assertFalse(data.get("serverFlying").getAsBoolean());

      try (JsonReader reader = new JsonReader(new StringReader(output))) {
        FlyStateUpdateEvent decoded = assertInstanceOf(FlyStateUpdateEvent.class, reader.nextEvent());
        assertEquals(Boolean.TRUE, decoded.allowFlight());
        assertEquals(Float.valueOf(0.075f), decoded.flySpeed());
        assertEquals(gameMode, decoded.gameMode());
        assertEquals(Boolean.TRUE, decoded.clientFlying());
        assertEquals(Boolean.FALSE, decoded.serverFlying());
        assertEquals(42, decoded.offset());
        assertNull(reader.nextEvent());
      }
    }
  }

  @Test
  void preservesUnknownValuesForOmittedAndExplicitNullFields() throws Exception {
    String[] records = {
      "{\"type\":\"player.fly_state_update\",\"data\":{}}\n",
      "{\"type\":\"player.fly_state_update\",\"data\":{\"allowFlight\":null,\"flySpeed\":null,"
        + "\"gameMode\":null,\"clientFlying\":null,\"serverFlying\":null}}\n",
      write(new FlyStateUpdateEvent()),
      write(new FlyStateUpdateEvent(null, null, null, null, null))
    };
    for (String record : records) {
      try (JsonReader reader = new JsonReader(new StringReader(record))) {
        FlyStateUpdateEvent decoded = assertInstanceOf(FlyStateUpdateEvent.class, reader.nextEvent());
        assertNull(decoded.allowFlight());
        assertNull(decoded.flySpeed());
        assertNull(decoded.gameMode());
        assertNull(decoded.clientFlying());
        assertNull(decoded.serverFlying());
        JsonObject data = JsonParser.parseString(write(decoded)).getAsJsonObject().getAsJsonObject("data");
        assertFalse(data.has("allowFlight"));
        assertFalse(data.has("flySpeed"));
        assertFalse(data.has("gameMode"));
        assertFalse(data.has("clientFlying"));
        assertFalse(data.has("serverFlying"));
        assertNull(reader.nextEvent());
      }
    }
  }

  @Test
  void preservesFalseAndZeroWithoutInferringFlightFromPermissionOrGameMode() throws Exception {
    FlyStateUpdateEvent event = new FlyStateUpdateEvent(false, 0.0f, "SURVIVAL", false, true);
    try (JsonReader reader = new JsonReader(new StringReader(write(event)))) {
      FlyStateUpdateEvent decoded = assertInstanceOf(FlyStateUpdateEvent.class, reader.nextEvent());
      assertEquals(Boolean.FALSE, decoded.allowFlight());
      assertEquals(Float.valueOf(0.0f), decoded.flySpeed());
      assertEquals("SURVIVAL", decoded.gameMode());
      assertEquals(Boolean.FALSE, decoded.clientFlying());
      assertEquals(Boolean.TRUE, decoded.serverFlying());
    }
  }

  @Test
  void preservesIndependentObservationsAndEventOrderAlongsideLegacyToggle() throws Exception {
    FlyStateUpdateEvent client = new FlyStateUpdateEvent(null, null, null, true, null);
    FlyStateUpdateEvent server = new FlyStateUpdateEvent(true, 0.05f, "CREATIVE", null, false);
    PlayerFlyToggleEvent legacy = new PlayerFlyToggleEvent(true);
    client.withOffset(7);
    server.withOffset(8);
    legacy.withOffset(9);
    try (JsonReader reader = new JsonReader(new StringReader(write(client, server, legacy)))) {
      FlyStateUpdateEvent first = assertInstanceOf(FlyStateUpdateEvent.class, reader.nextEvent());
      assertEquals(7, first.offset());
      assertEquals(Boolean.TRUE, first.clientFlying());
      assertNull(first.serverFlying());
      assertNull(first.allowFlight());
      assertNull(first.flySpeed());
      assertNull(first.gameMode());
      FlyStateUpdateEvent second = assertInstanceOf(FlyStateUpdateEvent.class, reader.nextEvent());
      assertEquals(8, second.offset());
      assertNull(second.clientFlying());
      assertEquals(Boolean.FALSE, second.serverFlying());
      assertEquals(Boolean.TRUE, second.allowFlight());
      assertEquals(Float.valueOf(0.05f), second.flySpeed());
      assertEquals("CREATIVE", second.gameMode());
      PlayerFlyToggleEvent third = assertInstanceOf(PlayerFlyToggleEvent.class, reader.nextEvent());
      assertTrue(third.isFlying());
      assertEquals(9, third.offset());
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void dispatchesThroughBothEntryPointsToTypedVisitor() {
    FlyStateUpdateEvent event = new FlyStateUpdateEvent(true, 0.05f, "CREATIVE", true, false);
    int[] visits = {0};
    EventSink sink = new EventSink() {
      @Override
      public void visit(FlyStateUpdateEvent received) {
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
