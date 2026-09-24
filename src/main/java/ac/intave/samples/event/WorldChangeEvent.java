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

import java.util.Objects;

/**
 * Changes the recorded player's world context. Emit before the destination world's
 * teleport, entity and block events. Consumers must discard the previous world's
 * entity and block context, even when both worlds have the same dimension type.
 */
public final class WorldChangeEvent extends Event {
  @SerializedName("fromWorld")
  private String fromWorld;
  @SerializedName("toWorld")
  private String toWorld;

  public WorldChangeEvent() {
  }

  /**
   * Use consistent, stable world identifiers (such as world UUID strings or unique
   * world keys), not dimension types. A null source establishes initial world context
   * or indicates that the previous world is unknown.
   */
  public WorldChangeEvent(String fromWorld, String toWorld) {
    this.fromWorld = fromWorld;
    this.toWorld = Objects.requireNonNull(toWorld, "toWorld");
  }

  /** Previous world identifier, or null if unknown. */
  public String fromWorld() {
    return fromWorld;
  }

  public String toWorld() {
    return toWorld;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
