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

/** A totem activation reported by an outgoing entity status packet. */
public final class TotemPopEvent extends Event {
  @SerializedName("entityId")
  private int entityId;

  public TotemPopEvent() {
  }

  public TotemPopEvent(int entityId) {
    this.entityId = entityId;
  }

  public int entityId() {
    return entityId;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
