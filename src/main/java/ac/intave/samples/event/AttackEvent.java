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

public final class AttackEvent extends Event {
  @SerializedName("source")
  private int source;
  @SerializedName("target")
  private int target;
  @SerializedName("attackStrength")
  private Float attackStrength;

  public AttackEvent() {
  }

  public AttackEvent(int source, int target) {
    this(source, target, null);
  }

  public AttackEvent(int source, int target, Float attackStrength) {
    this.source = source;
    this.target = target;
    this.attackStrength = attackStrength;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public int source() {
    return source;
  }

  public int target() {
    return target;
  }

  public static AttackEvent create(int source, int target) {
    return new AttackEvent(source, target);
  }

  /** Server-observed charge from 0 to 1 before attack processing; null when unknown. */
  public Float attackStrength() {
    return attackStrength;
  }

  public static AttackEvent create(int source, int target, Float attackStrength) {
    return new AttackEvent(source, target, attackStrength);
  }
}
