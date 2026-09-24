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

import com.google.gson.annotations.SerializedName;

/** A change to the recorded player's flight state, distinct from permission to fly or gliding. */
public final class PlayerFlyToggleEvent extends Event {
  @SerializedName("isFlying")
  private boolean isFlying;

  public PlayerFlyToggleEvent() {
  }

  public PlayerFlyToggleEvent(boolean isFlying) {
    this.isFlying = isFlying;
  }

  /** The resulting flight state; consumers should assign this value rather than invert their state. */
  public boolean isFlying() {
    return isFlying;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
