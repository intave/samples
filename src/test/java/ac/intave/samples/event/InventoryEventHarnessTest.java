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

import ac.intave.samples.share.Item;
import ac.intave.samples.share.ItemCategory;
import ac.intave.samples.share.SlotUpdate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class InventoryEventHarnessTest {
  private static final int CONTAINER_ID = 4;

  @Test
  void registersFourNewEventsWithoutReplacingLegacyEvents() {
    assertRegistered("inventory.open", InventoryOpenEvent.class);
    assertRegistered("inventory.action", InventoryActionEvent.class);
    assertRegistered("inventory.update", InventoryUpdateEvent.class);
    assertRegistered("inventory.close", InventoryCloseEvent.class);

    assertRegistered("window.click", WindowClickEvent.class);
    assertRegistered("window.items", WindowItemsEvent.class);
    assertRegistered("window.action", WindowActionEvent.class);
  }

  @Test
  void roundTripsACompleteInventoryStream() throws Exception {
    Item stone = new Item("STONE", 32, ItemCategory.BLOCK, false, 1.0, 1.0);
    InventoryEventHarness harness = new InventoryEventHarness();
    harness.emit(new InventoryOpenEvent(CONTAINER_ID, "minecraft:generic_9x3", false), 0);
    harness.emit(new InventoryUpdateEvent(
      CONTAINER_ID, true, 14,
      Arrays.asList(
        new SlotUpdate(0, stone),
        new SlotUpdate(27, null)
      ),
      true, null
    ), 3);
    harness.emit(new InventoryActionEvent(
      CONTAINER_ID, InventoryActionEvent.Action.QUICK_MOVE, 0, 0, 14,
      Arrays.asList(
        new SlotUpdate(0, null),
        new SlotUpdate(27, stone)
      ),
      true, null
    ), 17);
    harness.emit(new InventoryUpdateEvent(
      CONTAINER_ID, false, 15,
      Arrays.asList(
        new SlotUpdate(0, null),
        new SlotUpdate(27, stone)
      ),
      true, null
    ), 2);
    harness.emit(new InventoryCloseEvent(CONTAINER_ID, InventoryCloseEvent.Source.CLIENT), 41);

    String json = harness.serialize();
    assertTrue(json.contains("\"type\":\"inventory.open\""));
    assertTrue(json.contains("\"type\":\"inventory.update\""));

    List<Event> decoded = harness.roundTrip();
    assertEquals(5, decoded.size());

    InventoryOpenEvent open = assertInstanceOf(InventoryOpenEvent.class, decoded.get(0));
    assertEquals("minecraft:generic_9x3", open.menuType());
    assertFalse(open.inferred());

    InventoryUpdateEvent snapshot = assertInstanceOf(InventoryUpdateEvent.class, decoded.get(1));
    assertTrue(snapshot.full());
    assertEquals(14, snapshot.revision());
    assertEquals("STONE", snapshot.slots().get(0).item().type());
    assertNull(snapshot.slots().get(1).item(), "null inside an update means known empty");
    assertTrue(snapshot.carriedKnown());
    assertNull(snapshot.carriedItem());
    assertEquals(3, snapshot.offset());

    InventoryActionEvent action = assertInstanceOf(InventoryActionEvent.class, decoded.get(2));
    assertEquals(InventoryActionEvent.Action.QUICK_MOVE, action.action());
    assertEquals(0, action.slot());
    assertEquals(14, action.revision());
    assertEquals(2, action.predictedSlots().size());
    assertTrue(action.carriedKnown());

    InventoryUpdateEvent patch = assertInstanceOf(InventoryUpdateEvent.class, decoded.get(3));
    assertFalse(patch.full());
    assertEquals(15, patch.revision());

    InventoryCloseEvent close = assertInstanceOf(InventoryCloseEvent.class, decoded.get(4));
    assertEquals(InventoryCloseEvent.Source.CLIENT, close.source());
    assertEquals(41, close.offset());
  }

  @Test
  void translatesEveryContainerClickGestureAndKeepsRawFields() {
    List<ClickExpectation> expectations = Arrays.asList(
      expected(InventoryEventHarness.ContainerClickType.PICKUP, 12, 0, InventoryActionEvent.Action.CLICK_PRIMARY),
      expected(InventoryEventHarness.ContainerClickType.PICKUP, 12, 1, InventoryActionEvent.Action.CLICK_SECONDARY),
      expected(InventoryEventHarness.ContainerClickType.PICKUP, -999, 0, InventoryActionEvent.Action.DROP_CURSOR_STACK),
      expected(InventoryEventHarness.ContainerClickType.PICKUP, -999, 1, InventoryActionEvent.Action.DROP_CURSOR_ONE),
      expected(InventoryEventHarness.ContainerClickType.QUICK_MOVE, 12, 0, InventoryActionEvent.Action.QUICK_MOVE),
      expected(InventoryEventHarness.ContainerClickType.SWAP, 12, 2, InventoryActionEvent.Action.SWAP_HOTBAR),
      expected(InventoryEventHarness.ContainerClickType.SWAP, 12, 40, InventoryActionEvent.Action.SWAP_OFFHAND),
      expected(InventoryEventHarness.ContainerClickType.CLONE, 12, 2, InventoryActionEvent.Action.CLONE),
      expected(InventoryEventHarness.ContainerClickType.THROW, 12, 0, InventoryActionEvent.Action.DROP_SLOT_ONE),
      expected(InventoryEventHarness.ContainerClickType.THROW, 12, 1, InventoryActionEvent.Action.DROP_SLOT_STACK),
      expected(InventoryEventHarness.ContainerClickType.PICKUP_ALL, 12, 0, InventoryActionEvent.Action.COLLECT_TO_CURSOR),
      expected(InventoryEventHarness.ContainerClickType.QUICK_CRAFT, -999, 0, InventoryActionEvent.Action.DRAG_START),
      expected(InventoryEventHarness.ContainerClickType.QUICK_CRAFT, 12, 5, InventoryActionEvent.Action.DRAG_ADD_SLOT),
      expected(InventoryEventHarness.ContainerClickType.QUICK_CRAFT, -999, 10, InventoryActionEvent.Action.DRAG_END)
    );

    List<InventoryActionEvent.Action> actual = new ArrayList<>();
    for (ClickExpectation expectation : expectations) {
      InventoryActionEvent event = InventoryEventHarness.translateContainerClick(
        CONTAINER_ID, expectation.type,
        expectation.slot, expectation.button, -32768
      );
      actual.add(event.action());
      assertEquals(expectation.action, event.action());
      assertEquals(expectation.slot, event.slot());
      assertEquals(expectation.button, event.button());
      assertEquals(-32768, event.revision());
    }
    assertEquals(expectations.size(), actual.size());
  }

  @Test
  void allowsSmallNonClickActionsWithoutExtraPayloadTypes() throws Exception {
    InventoryActionEvent selected = InventoryActionEvent.simple(
      0, InventoryActionEvent.Action.SELECT_HOTBAR, -1, 4, null
    );
    InventoryActionEvent dropped = InventoryActionEvent.simple(
      0, InventoryActionEvent.Action.DROP_HELD_STACK, -1, -1, null
    );
    InventoryActionEvent picked = InventoryActionEvent.simple(
      0, InventoryActionEvent.Action.PICK_ITEM, 17, -1, null
    );

    assertEquals(4, selected.button());
    assertEquals(InventoryActionEvent.Action.DROP_HELD_STACK, dropped.action());
    assertEquals(17, picked.slot());
    assertTrue(selected.predictedSlots().isEmpty());
    assertFalse(selected.carriedKnown());
    assertNull(selected.revision());
  }

  @Test
  void rejectsBrokenStreamFraming() {
    InventoryEventHarness harness = new InventoryEventHarness();
    assertThrows(IllegalArgumentException.class, () -> harness.emit(
      InventoryActionEvent.simple(CONTAINER_ID, InventoryActionEvent.Action.CLICK_PRIMARY, 0, 0, null), 0
    ));

    harness.emit(new InventoryOpenEvent(CONTAINER_ID, "unknown", true), 0);
    assertThrows(IllegalArgumentException.class, () -> harness.emit(
      InventoryActionEvent.simple(CONTAINER_ID + 1, InventoryActionEvent.Action.CLICK_PRIMARY, 0, 0, null), 1
    ));
    harness.emit(new InventoryCloseEvent(CONTAINER_ID, InventoryCloseEvent.Source.INFERRED), 1);
    assertThrows(IllegalStateException.class, () -> harness.emit(
      new InventoryUpdateEvent(
        CONTAINER_ID, false, null, Collections.<SlotUpdate>emptyList(), false, null
      ), 1
    ));
  }

  @Test
  void dispatchesToTheSpecificInventoryVisitor() {
    InventoryActionEvent event = InventoryActionEvent.simple(
      CONTAINER_ID, InventoryActionEvent.Action.CLICK_PRIMARY, 0, 0, null
    );
    InventoryActionEvent[] visited = new InventoryActionEvent[1];
    EventSink sink = new EventSink() {
      @Override
      public void visit(InventoryActionEvent received) {
        visited[0] = received;
      }

      @Override
      public String name() {
        return "inventory-harness";
      }
    };

    event.accept(sink);

    assertSame(event, visited[0]);
  }

  private static void assertRegistered(String name, Class<? extends Event> eventClass) {
    assertEquals(eventClass, EventRegistry.typeNamed(name).eventClass());
  }

  private static ClickExpectation expected(
    InventoryEventHarness.ContainerClickType type,
    int slot, int button, InventoryActionEvent.Action action
  ) {
    return new ClickExpectation(type, slot, button, action);
  }

  private static final class ClickExpectation {
    private final InventoryEventHarness.ContainerClickType type;
    private final int slot;
    private final int button;
    private final InventoryActionEvent.Action action;

    private ClickExpectation(
      InventoryEventHarness.ContainerClickType type,
      int slot, int button, InventoryActionEvent.Action action
    ) {
      this.type = type;
      this.slot = slot;
      this.button = button;
      this.action = action;
    }
  }
}
