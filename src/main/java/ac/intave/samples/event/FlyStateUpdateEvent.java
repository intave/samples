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
 * A snapshot of the recorded player's flight abilities and observed flight states.
 * Emit at recording start and when any recorded value changes. All values describe
 * the state at this event's offset; null means unknown, not unchanged from a prior event.
 * Flight here is the flying ability, separate from gliding or simply being airborne.
 */
public final class FlyStateUpdateEvent extends Event {
  @SerializedName("allowFlight")
  private Boolean allowFlight;
  @SerializedName("flySpeed")
  private Float flySpeed;
  @SerializedName("gameMode")
  private String gameMode;
  @SerializedName("clientFlying")
  private Boolean clientFlying;
  @SerializedName("serverFlying")
  private Boolean serverFlying;

  public FlyStateUpdateEvent() {
  }

  /**
   * Pass null for unavailable values. Permission, speed and game mode come from
   * server state. Preserve client and server flight observations independently,
   * including disagreement; do not infer either from permission or game mode.
   */
  public FlyStateUpdateEvent(
    Boolean allowFlight, Float flySpeed, String gameMode,
    Boolean clientFlying, Boolean serverFlying
  ) {
    this.allowFlight = allowFlight;
    this.flySpeed = flySpeed;
    this.gameMode = gameMode;
    this.clientFlying = clientFlying;
    this.serverFlying = serverFlying;
  }

  /** Whether the server permits flight, independently of whether the player is flying. */
  public Boolean allowFlight() {
    return allowFlight;
  }

  /**
   * Server-configured raw player-abilities flying-speed value, not measured movement
   * speed. Producers using a differently scaled platform API must convert to this scale.
   */
  public Float flySpeed() {
    return flySpeed;
  }

  /**
   * Server game-mode name, such as SURVIVAL, CREATIVE, ADVENTURE or SPECTATOR.
   * Use the enum name rather than its ordinal; names are preserved verbatim so
   * future modes can be recorded without updating this library.
   */
  public String gameMode() {
    return gameMode;
  }

  /** Client-reported flying flag; this does not imply that the server accepted it. */
  public Boolean clientFlying() {
    return clientFlying;
  }

  /** Server-confirmed flying flag; this does not imply client acknowledgement. */
  public Boolean serverFlying() {
    return serverFlying;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
