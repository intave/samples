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

import ac.intave.samples.share.Position;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Bounds on an entity's possible position in the current world context.
 * These are absolute position bounds, not the entity's physical hitbox.
 */
public final class EntityPositionUncertaintyEvent extends Event {
  @SerializedName("entityId")
  private int entityId;
  @SerializedName("minPosition")
  private Position minPosition;
  @SerializedName("maxPosition")
  private Position maxPosition;

  public EntityPositionUncertaintyEvent() {
  }

  public EntityPositionUncertaintyEvent(int entityId, Position minPosition, Position maxPosition) {
    this.entityId = entityId;
    this.minPosition = Objects.requireNonNull(minPosition, "minPosition");
    this.maxPosition = Objects.requireNonNull(maxPosition, "maxPosition");
  }

  public int entityId() {
    return entityId;
  }

  public Position minPosition() {
    return minPosition;
  }

  public Position maxPosition() {
    return maxPosition;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
