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
import ac.intave.samples.share.SlotUpdate;
import com.google.gson.annotations.SerializedName;

import java.util.*;

/** A player inventory action normalized from a serverbound packet. */
public final class InventoryActionEvent extends Event {
  @SerializedName("containerId")
  private int containerId;
  @SerializedName("action")
  private Action action = Action.UNKNOWN;
  @SerializedName("slot")
  private int slot = -1;
  @SerializedName("button")
  private int button = -1;
  @SerializedName("revision")
  private Integer revision;
  @SerializedName("predictedSlots")
  private List<SlotUpdate> predictedSlots = new ArrayList<>();
  @SerializedName("carriedKnown")
  private boolean carriedKnown;
  @SerializedName("carriedItem")
  private Item carriedItem;

  public InventoryActionEvent() {
  }

  public InventoryActionEvent(
    int containerId, Action action, int slot, int button, Integer revision,
    Collection<SlotUpdate> predictedSlots,
    boolean carriedKnown, Item carriedItem
  ) {
    this.containerId = containerId;
    this.action = Objects.requireNonNull(action, "action");
    this.slot = slot;
    this.button = button;
    this.revision = revision;
    this.predictedSlots = new ArrayList<>(Objects.requireNonNull(predictedSlots, "predictedSlots"));
    this.carriedKnown = carriedKnown;
    this.carriedItem = carriedItem;
  }

  public static InventoryActionEvent simple(
    int containerId, Action action, int slot, int button, Integer revision
  ) {
    return new InventoryActionEvent(
      containerId, action, slot, button, revision,
      Collections.<SlotUpdate>emptyList(), false, null
    );
  }

  public int containerId() {
    return containerId;
  }

  public Action action() {
    return action;
  }

  public int slot() {
    return slot;
  }

  public int button() {
    return button;
  }

  /** Legacy action number or modern state ID, selected using the recording protocol version. */
  public Integer revision() {
    return revision;
  }

  public List<SlotUpdate> predictedSlots() {
    if (predictedSlots == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(predictedSlots);
  }

  public boolean carriedKnown() {
    return carriedKnown;
  }

  /** Null means empty when {@link #carriedKnown()} is true, otherwise unknown. */
  public Item carriedItem() {
    return carriedItem;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public enum Action {
    CLICK_PRIMARY,
    CLICK_SECONDARY,
    QUICK_MOVE,
    SWAP_HOTBAR,
    SWAP_OFFHAND,
    CLONE,
    DROP_SLOT_ONE,
    DROP_SLOT_STACK,
    DROP_CURSOR_ONE,
    DROP_CURSOR_STACK,
    DRAG_START,
    DRAG_ADD_SLOT,
    DRAG_END,
    COLLECT_TO_CURSOR,
    SELECT_HOTBAR,
    DROP_HELD_ONE,
    DROP_HELD_STACK,
    SWAP_HANDS,
    CREATIVE_SET_SLOT,
    CREATIVE_DROP,
    MENU_BUTTON,
    PLACE_RECIPE,
    PICK_ITEM,
    UNKNOWN
  }
}
