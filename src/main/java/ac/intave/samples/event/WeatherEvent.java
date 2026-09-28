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

/**
 * A snapshot of the recorded player's weather in the current world, including
 * player-specific overrides. Null fields are unknown, not clear weather or an
 * instruction to retain the previous value. Rain may render as snow or be hidden
 * depending on the biome and dimension; it is not a separate weather type.
 */
public final class WeatherEvent extends Event {
  @SerializedName("raining")
  private Boolean raining;
  @SerializedName("thundering")
  private Boolean thundering;
  @SerializedName("rainLevel")
  private Float rainLevel;
  @SerializedName("thunderLevel")
  private Float thunderLevel;

  public WeatherEvent() {
  }

  public WeatherEvent(Boolean raining, Boolean thundering, Float rainLevel, Float thunderLevel) {
    this.raining = raining;
    this.thundering = thundering;
    this.rainLevel = rainLevel;
    this.thunderLevel = thunderLevel;
  }

  public Boolean raining() {
    return raining;
  }

  /** Logical storm flag, when known; not inferred from a fading thunder level. */
  public Boolean thundering() {
    return thundering;
  }

  /** Client rain strength, normally 0 to 1, including transitions. */
  public Float rainLevel() {
    return rainLevel;
  }

  /** Client thunder strength before multiplication by rain strength, normally 0 to 1. */
  public Float thunderLevel() {
    return thunderLevel;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
