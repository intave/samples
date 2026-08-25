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

/** A full inventory snapshot or a server slot patch. */
public final class InventoryUpdateEvent extends Event {
  @SerializedName("containerId")
  private int containerId;
  @SerializedName("full")
  private boolean full;
  @SerializedName("revision")
  private Integer revision;
  @SerializedName("slots")
  private List<SlotUpdate> slots = new ArrayList<>();
  @SerializedName("carriedKnown")
  private boolean carriedKnown;
  @SerializedName("carriedItem")
  private Item carriedItem;

  public InventoryUpdateEvent() {
  }

  public InventoryUpdateEvent(
    int containerId, boolean full, Integer revision,
    Collection<SlotUpdate> slots,
    boolean carriedKnown, Item carriedItem
  ) {
    this.containerId = containerId;
    this.full = full;
    this.revision = revision;
    this.slots = new ArrayList<>(Objects.requireNonNull(slots, "slots"));
    this.carriedKnown = carriedKnown;
    this.carriedItem = carriedItem;
  }

  public int containerId() {
    return containerId;
  }

  public boolean full() {
    return full;
  }

  public Integer revision() {
    return revision;
  }

  public List<SlotUpdate> slots() {
    if (slots == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(slots);
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
}
