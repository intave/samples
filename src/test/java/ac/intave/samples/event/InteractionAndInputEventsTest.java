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
import ac.intave.samples.share.Hand;
import ac.intave.samples.share.Position;
import ac.intave.samples.share.Rotation;
import ac.intave.samples.share.Vector3d;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

final class InteractionAndInputEventsTest {
  @Test
  void roundTripsBothEntityInteractionActions() throws Exception {
    EntityInteractEvent interact = new EntityInteractEvent(12, 34, EntityInteractEvent.Action.INTERACT, Hand.MAIN_HAND);
    EntityInteractEvent at = new EntityInteractEvent(
      12, 35, EntityInteractEvent.Action.INTERACT_AT, Hand.OFF_HAND, new Vector3d(-0.25, 1.5, 0.125)
    );
    at.withOffset(8);
    String output = write(interact, at);
    JsonObject record = JsonParser.parseString(output.split("\n")[1]).getAsJsonObject();
    assertEquals("entity.interact", record.get("type").getAsString());
    assertEquals("INTERACT_AT", record.getAsJsonObject("data").get("action").getAsString());
    assertFalse(record.getAsJsonObject("data").has("targetType"));

    try (JsonReader reader = new JsonReader(new StringReader(output))) {
      EntityInteractEvent first = assertInstanceOf(EntityInteractEvent.class, reader.nextEvent());
      assertEquals(12, first.source());
      assertEquals(34, first.target());
      assertEquals(EntityInteractEvent.Action.INTERACT, first.action());
      assertEquals(Hand.MAIN_HAND, first.hand());
      assertNull(first.hitPosition());
      EntityInteractEvent second = assertInstanceOf(EntityInteractEvent.class, reader.nextEvent());
      assertEquals(12, second.source());
      assertEquals(35, second.target());
      assertEquals(EntityInteractEvent.Action.INTERACT_AT, second.action());
      assertEquals(Hand.OFF_HAND, second.hand());
      assertEquals(new Vector3d(-0.25, 1.5, 0.125), second.hitPosition());
      assertEquals(8, second.offset());
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void roundTripsAttackStrengthIncludingKnownZero() throws Exception {
    String output = write(AttackEvent.create(12, 34, 0.0f), new AttackEvent(12, 35, 0.75f));
    JsonObject data = JsonParser.parseString(output.split("\n")[0]).getAsJsonObject().getAsJsonObject("data");
    assertEquals(0.0f, data.get("attackStrength").getAsFloat());
    try (JsonReader reader = new JsonReader(new StringReader(output))) {
      AttackEvent first = assertInstanceOf(AttackEvent.class, reader.nextEvent());
      assertEquals(12, first.source());
      assertEquals(34, first.target());
      assertEquals(Float.valueOf(0.0f), first.attackStrength());
      AttackEvent second = assertInstanceOf(AttackEvent.class, reader.nextEvent());
      assertEquals(Float.valueOf(0.75f), second.attackStrength());
    }
  }

  @Test
  void preservesSwingHandsAndIndependentTiming() throws Exception {
    ClickEvent first = ClickEvent.create(Hand.MAIN_HAND);
    ClickEvent second = ClickEvent.create(Hand.OFF_HAND);
    first.withOffset(10);
    second.withOffset(20);
    try (JsonReader reader = new JsonReader(new StringReader(write(first, second)))) {
      ClickEvent decodedFirst = assertInstanceOf(ClickEvent.class, reader.nextEvent());
      ClickEvent decodedSecond = assertInstanceOf(ClickEvent.class, reader.nextEvent());
      assertEquals(Hand.MAIN_HAND, decodedFirst.hand());
      assertEquals(Hand.OFF_HAND, decodedSecond.hand());
      assertEquals(10, decodedFirst.offset());
      assertEquals(20, decodedSecond.offset());
    }
    ClickEvent legacyFirst = ClickEvent.create();
    ClickEvent legacySecond = ClickEvent.create();
    assertNotSame(legacyFirst, legacySecond);
    legacyFirst.withOffset(7);
    legacySecond.withOffset(19);
    assertEquals(7, legacyFirst.offset());
    assertNull(legacyFirst.hand());
  }

  @Test
  void preservesSprintTrueFalseAndUnknownInSerializationAndEquality() throws Exception {
    PlayerMoveEvent sprinting = movement(true);
    PlayerMoveEvent walking = movement(false);
    PlayerMoveEvent unknown = movement(null);
    assertEquals(3, new HashSet<>(Arrays.asList(sprinting, walking, unknown)).size());
    assertNotEquals(sprinting, walking);
    assertNotEquals(walking, unknown);
    assertTrue(sprinting.toString().contains("sprinting=true"));
    String output = write(sprinting, walking, unknown);
    JsonObject data = JsonParser.parseString(output.split("\n")[1]).getAsJsonObject().getAsJsonObject("data");
    assertFalse(data.get("sprinting").getAsBoolean());
    try (JsonReader reader = new JsonReader(new StringReader(output))) {
      for (PlayerMoveEvent expected : Arrays.asList(sprinting, walking, unknown)) {
        PlayerMoveEvent decoded = assertInstanceOf(PlayerMoveEvent.class, reader.nextEvent());
        assertEquals(expected.sprinting(), decoded.sprinting());
        assertEquals(expected, decoded);
        assertEquals(expected.hashCode(), decoded.hashCode());
      }
    }
  }

  @Test
  void readsOlderRecordsAndConstructorsWithUnknownNewFields() throws Exception {
    String legacy = "{\"type\":\"combat.attack\",\"data\":{\"source\":12,\"target\":34}}\n"
      + "{\"type\":\"input.click\",\"data\":{}}\n"
      + "{\"type\":\"player.move\",\"data\":{}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(legacy))) {
      assertNull(assertInstanceOf(AttackEvent.class, reader.nextEvent()).attackStrength());
      assertNull(assertInstanceOf(ClickEvent.class, reader.nextEvent()).hand());
      assertNull(assertInstanceOf(PlayerMoveEvent.class, reader.nextEvent()).sprinting());
    }
    assertNull(AttackEvent.create(12, 34).attackStrength());
    PlayerMoveEvent old = PlayerMoveEvent.create(0, 1, Position.ZERO, Rotation.ZERO,
      false, true, false, false, false, false, false, false);
    assertNull(old.sprinting());
    assertEquals(movement(null), old);
    PlayerMoveEvent coordinates = PlayerMoveEvent.create(0, 1, 0, 0, 0, 0, 0,
      false, true, false, false, false, false, false, false, true);
    assertEquals(movement(true), coordinates);
  }

  @Test
  void preservesClientSneakingIndependentlyOfSneakingAndSprinting() throws Exception {
    for (Boolean clientSneaking : Arrays.asList(Boolean.TRUE, Boolean.FALSE, null)) {
      PlayerMoveEvent event = PlayerMoveEvent.create(0, 1, Position.ZERO, Rotation.ZERO,
        false, true, false, false, false, false, false, false, true, clientSneaking);
      PlayerMoveEvent coordinates = PlayerMoveEvent.create(0, 1, 0, 0, 0, 0, 0,
        false, true, false, false, false, false, false, false, true, clientSneaking);
      assertEquals(event, coordinates);
      assertTrue(event.toString().contains("clientSneaking=" + clientSneaking));
      String output = write(event);
      JsonObject data = JsonParser.parseString(output).getAsJsonObject().getAsJsonObject("data");
      if (clientSneaking == null) {
        assertFalse(data.has("clientSneaking"));
      } else {
        assertEquals(clientSneaking.booleanValue(), data.get("clientSneaking").getAsBoolean());
      }
      try (JsonReader reader = new JsonReader(new StringReader(output))) {
        PlayerMoveEvent decoded = assertInstanceOf(PlayerMoveEvent.class, reader.nextEvent());
        assertEquals(clientSneaking, decoded.clientSneaking());
        assertFalse(decoded.sneaking());
        assertEquals(Boolean.TRUE, decoded.sprinting());
        assertEquals(event, decoded);
        assertEquals(event.hashCode(), decoded.hashCode());
      }
      if (clientSneaking != null) {
        assertNotEquals(movement(true), event);
      }
    }
    PlayerMoveEvent sneaking = PlayerMoveEvent.create(0, 1, Position.ZERO, Rotation.ZERO,
      false, true, false, false, false, true, false, false, false, false);
    assertTrue(sneaking.sneaking());
    assertEquals(Boolean.FALSE, sneaking.clientSneaking());
  }

  @Test
  void olderMovementSamplesDoNotInferClientSneakingFromSneaking() throws Exception {
    String legacy = "{\"type\":\"player.move\",\"data\":{\"sneaking\":true}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(legacy))) {
      PlayerMoveEvent decoded = assertInstanceOf(PlayerMoveEvent.class, reader.nextEvent());
      assertTrue(decoded.sneaking());
      assertNull(decoded.clientSneaking());
    }
    assertNull(movement(true).clientSneaking());
    assertNull(new PlayerMoveEvent().clientSneaking());
    PlayerMoveEvent unknownState = new PlayerMoveEvent(Position.ZERO, Rotation.ZERO, 1,
      false, true, false, false, false, false, false, false, null, null, null);
    assertNull(unknownState.clientSneaking());
    assertEquals(movement(null), unknownState);
  }

  @Test
  void dispatchesEntityInteractionsThroughBothTypedEntryPoints() {
    EntityInteractEvent event = new EntityInteractEvent();
    int[] count = {0};
    EventSink sink = new EventSink() {
      @Override
      public void visit(EntityInteractEvent received) {
        assertSame(event, received);
        count[0]++;
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
    assertEquals(2, count[0]);
  }

  private PlayerMoveEvent movement(Boolean sprinting) {
    return PlayerMoveEvent.create(0, 1, Position.ZERO, Rotation.ZERO,
      false, true, false, false, false, false, false, false, sprinting);
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
