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

package ac.intave.samples.share;

import com.google.gson.annotations.SerializedName;

/** Player-visible state of a named clock from a Minecraft 26.1+ time packet. */
public final class ClockState {
  @SerializedName("time")
  private long time;
  @SerializedName("partialTick")
  private float partialTick;
  @SerializedName("rate")
  private float rate;

  public ClockState() {
  }

  public ClockState(long time, float partialTick, float rate) {
    this.time = time;
    this.partialTick = partialTick;
    this.rate = rate;
  }

  public long time() {
    return time;
  }

  public float partialTick() {
    return partialTick;
  }

  /** Clock ticks per game tick. Zero includes a paused clock. */
  public float rate() {
    return rate;
  }
}
