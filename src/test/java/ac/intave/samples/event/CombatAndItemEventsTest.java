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
import ac.intave.samples.share.Item;
import ac.intave.samples.share.ItemCategory;
import ac.intave.samples.share.Position;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class CombatAndItemEventsTest {
  @Test
  void roundTripsTotemPop() throws Exception {
    TotemPopEvent event = new TotemPopEvent(17);
    event.withOffset(42);

    TotemPopEvent decoded = roundTrip(event, "entity.totem_pop");

    assertEquals(17, decoded.entityId());
    assertEquals(42, decoded.offset());
  }

  @Test
  void roundTripsDamageAttributionWithoutConflatingSources() throws Exception {
    DamageEvent event = new DamageEvent(17, "minecraft:arrow", 0, 29, new Position(1, 2, 3));
    event.withOffset(5);

    DamageEvent decoded = roundTrip(event, "combat.damage");

    assertEquals(17, decoded.target());
    assertEquals("minecraft:arrow", decoded.damageType());
    assertEquals(Integer.valueOf(0), decoded.causeEntityId());
    assertEquals(Integer.valueOf(29), decoded.directEntityId());
    assertEquals(new Position(1, 2, 3), decoded.sourcePosition());
    assertEquals(5, decoded.offset());
  }

  @Test
  void readsDamageWithoutOptionalSources() throws Exception {
    try (JsonReader reader = new JsonReader(new StringReader(
      "{\"type\":\"combat.damage\",\"data\":{\"target\":17,\"damageType\":\"minecraft:fall\"}}\n"
    ))) {
      DamageEvent decoded = assertInstanceOf(DamageEvent.class, reader.nextEvent());
      assertNull(decoded.causeEntityId());
      assertNull(decoded.directEntityId());
      assertNull(decoded.sourcePosition());
      DamageEvent roundTripped = roundTrip(decoded, "combat.damage");
      assertNull(roundTripped.causeEntityId());
      assertNull(roundTripped.directEntityId());
      assertNull(roundTripped.sourcePosition());
    }
  }

  @Test
  void roundTripsItemActionsAndSnapshots() throws Exception {
    Item item = new Item("ENDER_PEARL", 12, ItemCategory.OTHER, true, 1.5, 2.5);
    for (ItemActionEvent.Action action : ItemActionEvent.Action.values()) {
      Float strength = action == ItemActionEvent.Action.STAB ? 0.0f : null;
      ItemActionEvent event = new ItemActionEvent(action, Hand.OFF_HAND, item, strength);
      event.withOffset(9);

      ItemActionEvent decoded = roundTrip(event, "item.action");

      assertEquals(action, decoded.action());
      assertEquals(Hand.OFF_HAND, decoded.hand());
      assertEquals("ENDER_PEARL", decoded.item().type());
      assertEquals(12, decoded.item().amount());
      assertEquals(ItemCategory.OTHER, decoded.item().category());
      assertTrue(decoded.item().glowing());
      assertEquals(1.5, decoded.item().baseQuality());
      assertEquals(2.5, decoded.item().enchantmentQuality());
      assertEquals(strength, decoded.attackStrength());
      assertEquals(9, decoded.offset());
    }
  }

  @Test
  void distinguishesUnknownItemContextFromAnEmptyHand() throws Exception {
    ItemActionEvent unknown = roundTrip(
      new ItemActionEvent(ItemActionEvent.Action.RELEASE_USE, null, null), "item.action"
    );
    assertNull(unknown.hand());
    assertNull(unknown.item());
    assertNull(unknown.attackStrength());

    ItemActionEvent empty = roundTrip(
      new ItemActionEvent(ItemActionEvent.Action.USE, Hand.MAIN_HAND, Item.air()), "item.action"
    );
    assertEquals(Hand.MAIN_HAND, empty.hand());
    assertEquals("AIR", empty.item().type());
    assertEquals(0, empty.item().amount());
  }

  @Test
  void dispatchesThroughAcceptAndVisitSelectToTypedVisitors() {
    List<Event> received = new ArrayList<>();
    EventSink sink = new EventSink() {
      @Override
      public void visit(TotemPopEvent event) {
        received.add(event);
      }

      @Override
      public void visit(DamageEvent event) {
        received.add(event);
      }

      @Override
      public void visit(ItemActionEvent event) {
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

    for (Event event : Arrays.asList(new TotemPopEvent(), new DamageEvent(), new ItemActionEvent())) {
      event.accept(sink);
      sink.visitSelect(event);
      assertSame(event, received.get(received.size() - 2));
      assertSame(event, received.get(received.size() - 1));
    }
    assertEquals(6, received.size());
  }

  private <T extends Event> T roundTrip(T event, String typeName) throws Exception {
    assertEquals(typeName, EventRegistry.typeOf(event).name());
    assertEquals(event.getClass(), EventRegistry.typeNamed(typeName).eventClass());
    StringWriter output = new StringWriter();
    try (JsonWriter writer = new JsonWriter(output)) {
      // Exercise typed visitor fallback to JsonWriter.visitAny as well as serialization.
      event.accept(writer);
    }
    try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
      Event decoded = reader.nextEvent();
      assertInstanceOf(event.getClass(), decoded);
      assertNull(reader.nextEvent());
      @SuppressWarnings("unchecked")
      T result = (T) decoded;
      return result;
    }
  }
}
