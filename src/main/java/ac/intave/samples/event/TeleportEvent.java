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
import ac.intave.samples.share.Rotation;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * A server-issued teleport or position correction for the recorded player in the
 * current world context. This event does not imply client acknowledgement.
 * For a world transition, emit {@link WorldChangeEvent} first.
 */
public final class TeleportEvent extends Event {
  @SerializedName("position")
  private Position position;
  @SerializedName("rotation")
  private Rotation rotation;
  @SerializedName("teleportId")
  private Integer teleportId;

  public TeleportEvent() {
  }

  public TeleportEvent(Position position, Rotation rotation) {
    this(position, rotation, null);
  }

  /** Position and rotation must be absolute, with any packet-relative flags resolved. */
  public TeleportEvent(Position position, Rotation rotation, Integer teleportId) {
    this.position = Objects.requireNonNull(position, "position");
    this.rotation = Objects.requireNonNull(rotation, "rotation");
    this.teleportId = teleportId;
  }

  public Position position() {
    return position;
  }

  public Rotation rotation() {
    return rotation;
  }

  /** Packet teleport ID, or null when unavailable. Zero is a valid ID. */
  public Integer teleportId() {
    return teleportId;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
