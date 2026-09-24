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

import ac.intave.samples.share.Hand;
import ac.intave.samples.share.Item;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public final class ItemActionEvent extends Event {
  @SerializedName("action")
  private Action action = Action.UNKNOWN;
  @SerializedName("hand")
  private Hand hand;
  @SerializedName("item")
  private Item item;
  @SerializedName("attackStrength")
  private Float attackStrength;

  public ItemActionEvent() {
  }

  public ItemActionEvent(Action action, Hand hand, Item item) {
    this(action, hand, item, null);
  }

  public ItemActionEvent(Action action, Hand hand, Item item, Float attackStrength) {
    this.action = Objects.requireNonNull(action, "action");
    this.hand = hand;
    this.item = item;
    this.attackStrength = attackStrength;
  }

  public Action action() {
    return action;
  }

  /** The packet hand or resolved active hand; null when unknown. */
  public Hand hand() {
    return hand;
  }

  /** Item snapshot before the action; null means unknown, while {@link Item#air()} means empty. */
  public Item item() {
    return item;
  }

  /** Server-observed charge from 0 to 1 before stab processing; null if unavailable or not applicable. */
  public Float attackStrength() {
    return attackStrength;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public enum Action {
    USE,
    RELEASE_USE,
    STAB,
    UNKNOWN
  }
}
