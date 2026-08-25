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

package ac.intave.samples.share;

import com.google.gson.annotations.SerializedName;

/** A slot value from a snapshot, patch, or client prediction. */
public final class SlotUpdate {
  @SerializedName("slot")
  private int slot;
  @SerializedName("item")
  private Item item;

  public SlotUpdate() {
  }

  public SlotUpdate(int slot, Item item) {
    this.slot = slot;
    this.item = item;
  }

  public int slot() {
    return slot;
  }

  /** Null represents a known empty slot; an absent update represents unknown state. */
  public Item item() {
    return item;
  }
}
