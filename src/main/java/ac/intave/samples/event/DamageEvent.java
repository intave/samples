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

/** A server-reported damage notification; it does not contain a damage amount. */
public final class DamageEvent extends Event {
  @SerializedName("target")
  private int target;
  @SerializedName("damageType")
  private String damageType;
  @SerializedName("causeEntityId")
  private Integer causeEntityId;
  @SerializedName("directEntityId")
  private Integer directEntityId;
  @SerializedName("sourcePosition")
  private Position sourcePosition;

  public DamageEvent() {
  }

  public DamageEvent(
    int target, String damageType, Integer causeEntityId,
    Integer directEntityId, Position sourcePosition
  ) {
    this.target = target;
    this.damageType = Objects.requireNonNull(damageType, "damageType");
    this.causeEntityId = causeEntityId;
    this.directEntityId = directEntityId;
    this.sourcePosition = sourcePosition;
  }

  public int target() {
    return target;
  }

  /** Namespaced damage type, such as {@code minecraft:arrow}, rather than a registry ID. */
  public String damageType() {
    return damageType;
  }

  /** Causing entity (for example, the shooter), or null if absent or unknown. Uses the actual entity ID, without packet encoding offsets. */
  public Integer causeEntityId() {
    return causeEntityId;
  }

  /** Direct source (for example, the projectile), or null if absent or unknown. Uses the actual entity ID, without packet encoding offsets. */
  public Integer directEntityId() {
    return directEntityId;
  }

  /** Source position supplied by the packet, or null when unavailable. */
  public Position sourcePosition() {
    return sourcePosition;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
