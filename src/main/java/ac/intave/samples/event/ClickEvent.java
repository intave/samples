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
import com.google.gson.annotations.SerializedName;

/** An arm-swing input, not confirmation of an attack or hit. */
public final class ClickEvent extends Event {
  @SerializedName("hand")
  private Hand hand;

  public ClickEvent() {
  }

  public ClickEvent(Hand hand) {
    this.hand = hand;
  }

  /** Swing hand, or null when unknown (including older recordings). */
  public Hand hand() {
    return hand;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public static ClickEvent create() {
    return new ClickEvent();
  }

  public static ClickEvent create(Hand hand) {
    return new ClickEvent(hand);
  }
}
