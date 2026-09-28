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

import ac.intave.samples.share.ClockState;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The time state sent to the recorded player. Null fields are unknown. The event
 * deliberately excludes server/world age because recordings do not identify worlds.
 * Protocols through 1.21.11 use {@link #time()} and {@link #ticking()}; Minecraft
 * 26.1 and later uses the player-specific named states in {@link #clocks()}.
 */
public final class TimeEvent extends Event {
  @SerializedName(value = "time", alternate = "dayTime")
  private Long time;
  @SerializedName(value = "ticking", alternate = "tickDayTime")
  private Boolean ticking;
  @SerializedName("clocks")
  private Map<String, ClockState> clocks;

  public TimeEvent() {
  }

  /** Normalized player time: the sign does not encode whether time advances. */
  public TimeEvent(Long time, Boolean ticking) {
    this(time, ticking, null);
  }

  /** Clock keys are namespaced registry names from this player's outgoing packet. */
  public TimeEvent(Long time, Boolean ticking, Map<String, ClockState> clocks) {
    this.time = time;
    this.ticking = ticking;
    this.clocks = clocks == null ? null : new LinkedHashMap<>(clocks);
  }

  /**
   * Decodes the pre-1.21.2 signed time packet. A negative value freezes the clock.
   * The frozen-zero sentinel -1 is observed by the client as tick 1 and retained
   * that way here. Long.MIN_VALUE cannot be represented as a positive long.
   */
  public static TimeEvent fromLegacyPacket(long signedTime) {
    if (signedTime == Long.MIN_VALUE) {
      throw new IllegalArgumentException("Legacy day time magnitude exceeds Long.MAX_VALUE");
    }
    return new TimeEvent(Math.abs(signedTime), signedTime >= 0);
  }

  /** Full client-visible time including whole days; not reduced modulo 24000. */
  public Long time() {
    return time;
  }

  public Boolean ticking() {
    return ticking;
  }

  /** All known player-visible clocks, or null on protocols without named clocks. */
  public Map<String, ClockState> clocks() {
    return clocks == null ? null : Collections.unmodifiableMap(clocks);
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
