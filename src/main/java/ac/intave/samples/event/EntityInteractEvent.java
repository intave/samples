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
import ac.intave.samples.share.Vector3d;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/** A client entity-interaction request, not confirmation of its outcome. */
public final class EntityInteractEvent extends Event {
  @SerializedName("source")
  private int source;
  @SerializedName("target")
  private int target;
  @SerializedName("action")
  private Action action = Action.UNKNOWN;
  @SerializedName("hand")
  private Hand hand;
  @SerializedName("hitPosition")
  private Vector3d hitPosition;

  public EntityInteractEvent() {
  }

  public EntityInteractEvent(int source, int target, Action action, Hand hand) {
    this(source, target, action, hand, null);
  }

  public EntityInteractEvent(int source, int target, Action action, Hand hand, Vector3d hitPosition) {
    this.source = source;
    this.target = target;
    this.action = Objects.requireNonNull(action, "action");
    this.hand = hand;
    this.hitPosition = hitPosition;
  }

  public int source() {
    return source;
  }

  public int target() {
    return target;
  }

  public Action action() {
    return action;
  }

  /** Interaction hand, or null when unknown. */
  public Hand hand() {
    return hand;
  }

  /** Entity-relative hit position for INTERACT_AT; null when unavailable or not applicable. */
  public Vector3d hitPosition() {
    return hitPosition;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public enum Action {
    INTERACT,
    INTERACT_AT,
    UNKNOWN
  }
}
