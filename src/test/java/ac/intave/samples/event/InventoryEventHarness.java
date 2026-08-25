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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Test-only stream harness used until OpenIntave emits the new events. */
final class InventoryEventHarness {
  private final List<Event> events = new ArrayList<>();
  private Integer containerId;
  private boolean closed;

  void emit(Event event, long offset) {
    if (closed) {
      throw new IllegalStateException("Inventory stream is already closed");
    }
    if (events.isEmpty() && !(event instanceof InventoryOpenEvent)) {
      throw new IllegalArgumentException("First inventory event must be inventory.open");
    }

    int eventContainerId = containerIdOf(event);
    if (containerId == null) {
      containerId = eventContainerId;
    } else if (containerId != eventContainerId) {
      throw new IllegalArgumentException("Container ID changed within an inventory stream");
    }

    event.withOffset(offset);
    events.add(event);
    closed = event instanceof InventoryCloseEvent;
  }

  List<Event> events() {
    return Collections.unmodifiableList(events);
  }

  String serialize() throws IOException {
    StringWriter output = new StringWriter();
    JsonWriter writer = new JsonWriter(output);
    for (Event event : events) {
      writer.visitAny(event);
    }
    writer.flush();
    return output.toString();
  }

  List<Event> roundTrip() throws IOException {
    List<Event> decoded = new ArrayList<>();
    try (JsonReader reader = new JsonReader(new StringReader(serialize()))) {
      Event event;
      while ((event = reader.nextEvent()) != null) {
        decoded.add(event);
      }
    }
    return decoded;
  }

  static InventoryActionEvent translateContainerClick(
    int containerId, ContainerClickType clickType,
    int slot, int button, Integer revision
  ) {
    InventoryActionEvent.Action action;
    switch (clickType) {
      case PICKUP:
        if (slot == -999) {
          action = button == 0
            ? InventoryActionEvent.Action.DROP_CURSOR_STACK
            : button == 1
              ? InventoryActionEvent.Action.DROP_CURSOR_ONE
              : InventoryActionEvent.Action.UNKNOWN;
        } else {
          action = button == 0
            ? InventoryActionEvent.Action.CLICK_PRIMARY
            : button == 1
              ? InventoryActionEvent.Action.CLICK_SECONDARY
              : InventoryActionEvent.Action.UNKNOWN;
        }
        break;
      case QUICK_MOVE:
        action = InventoryActionEvent.Action.QUICK_MOVE;
        break;
      case SWAP:
        action = button >= 0 && button <= 8
          ? InventoryActionEvent.Action.SWAP_HOTBAR
          : button == 40
            ? InventoryActionEvent.Action.SWAP_OFFHAND
            : InventoryActionEvent.Action.UNKNOWN;
        break;
      case CLONE:
        action = InventoryActionEvent.Action.CLONE;
        break;
      case THROW:
        action = button == 0
          ? InventoryActionEvent.Action.DROP_SLOT_ONE
          : button == 1
            ? InventoryActionEvent.Action.DROP_SLOT_STACK
            : InventoryActionEvent.Action.UNKNOWN;
        break;
      case QUICK_CRAFT:
        switch (button & 3) {
          case 0:
            action = InventoryActionEvent.Action.DRAG_START;
            break;
          case 1:
            action = InventoryActionEvent.Action.DRAG_ADD_SLOT;
            break;
          case 2:
            action = InventoryActionEvent.Action.DRAG_END;
            break;
          default:
            action = InventoryActionEvent.Action.UNKNOWN;
        }
        break;
      case PICKUP_ALL:
        action = InventoryActionEvent.Action.COLLECT_TO_CURSOR;
        break;
      default:
        action = InventoryActionEvent.Action.UNKNOWN;
    }
    return InventoryActionEvent.simple(containerId, action, slot, button, revision);
  }

  private static int containerIdOf(Event event) {
    if (event instanceof InventoryOpenEvent) {
      return ((InventoryOpenEvent) event).containerId();
    }
    if (event instanceof InventoryActionEvent) {
      return ((InventoryActionEvent) event).containerId();
    }
    if (event instanceof InventoryUpdateEvent) {
      return ((InventoryUpdateEvent) event).containerId();
    }
    if (event instanceof InventoryCloseEvent) {
      return ((InventoryCloseEvent) event).containerId();
    }
    throw new IllegalArgumentException("Not an inventory event: " + event.getClass().getName());
  }

  enum ContainerClickType {
    PICKUP,
    QUICK_MOVE,
    SWAP,
    CLONE,
    THROW,
    QUICK_CRAFT,
    PICKUP_ALL
  }
}
