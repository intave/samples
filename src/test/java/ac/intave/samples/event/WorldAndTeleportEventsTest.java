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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class WorldAndTeleportEventsTest {
  @Test
  void roundTripsWorldTransitionBeforeTeleport() throws Exception {
    WorldChangeEvent world = new WorldChangeEvent("server:lobby", "server:arena");
    world.withOffset(12);
    TeleportEvent teleport = new TeleportEvent(new Position(-12.5, 80, 32.25), new Rotation(90, -30), 7);
    teleport.withOffset(3);
    String output = write(world, teleport);
    String[] records = output.split("\n");
    JsonObject worldRecord = JsonParser.parseString(records[0]).getAsJsonObject();
    assertEquals("player.world_change", worldRecord.get("type").getAsString());
    assertEquals("server:lobby", worldRecord.getAsJsonObject("data").get("fromWorld").getAsString());
    assertEquals("server:arena", worldRecord.getAsJsonObject("data").get("toWorld").getAsString());
    JsonObject teleportRecord = JsonParser.parseString(records[1]).getAsJsonObject();
    assertEquals("player.teleport", teleportRecord.get("type").getAsString());
    assertEquals(7, teleportRecord.getAsJsonObject("data").get("teleportId").getAsInt());

    try (JsonReader reader = new JsonReader(new StringReader(output))) {
      WorldChangeEvent decodedWorld = assertInstanceOf(WorldChangeEvent.class, reader.nextEvent());
      assertEquals("server:lobby", decodedWorld.fromWorld());
      assertEquals("server:arena", decodedWorld.toWorld());
      assertEquals(12, decodedWorld.offset());
      TeleportEvent decodedTeleport = assertInstanceOf(TeleportEvent.class, reader.nextEvent());
      assertEquals(new Position(-12.5, 80, 32.25), decodedTeleport.position());
      assertEquals(new Rotation(90, -30), decodedTeleport.rotation());
      assertEquals(Integer.valueOf(7), decodedTeleport.teleportId());
      assertEquals(3, decodedTeleport.offset());
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void preservesUnknownPreviousWorldAndDistinguishesMissingTeleportIdFromZero() throws Exception {
    String output = write(
      new WorldChangeEvent(null, "server:lobby"),
      new TeleportEvent(Position.ZERO, Rotation.ZERO),
      new TeleportEvent(Position.ZERO, Rotation.ZERO, 0)
    );
    try (JsonReader reader = new JsonReader(new StringReader(output))) {
      WorldChangeEvent world = assertInstanceOf(WorldChangeEvent.class, reader.nextEvent());
      assertNull(world.fromWorld());
      assertEquals("server:lobby", world.toWorld());
      TeleportEvent legacy = assertInstanceOf(TeleportEvent.class, reader.nextEvent());
      assertNull(legacy.teleportId());
      assertEquals(Position.ZERO, legacy.position());
      assertEquals(Rotation.ZERO, legacy.rotation());
      TeleportEvent modern = assertInstanceOf(TeleportEvent.class, reader.nextEvent());
      assertEquals(Integer.valueOf(0), modern.teleportId());
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void dispatchesBothEventsThroughTypedVisitors() {
    List<Event> received = new ArrayList<>();
    EventSink sink = new EventSink() {
      @Override
      public void visit(WorldChangeEvent event) {
        received.add(event);
      }

      @Override
      public void visit(TeleportEvent event) {
        received.add(event);
      }

      @Override
      public void visitAny(Event event) {
        fail("Typed visitor was bypassed");
      }

      @Override
      public String name() {
        return "test";
      }
    };
    for (Event event : Arrays.asList(new WorldChangeEvent(), new TeleportEvent())) {
      event.accept(sink);
      sink.visitSelect(event);
      assertSame(event, received.get(received.size() - 2));
      assertSame(event, received.get(received.size() - 1));
    }
    assertEquals(4, received.size());
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
