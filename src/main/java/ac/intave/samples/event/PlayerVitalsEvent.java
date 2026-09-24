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
 * A combined snapshot of the recorded player's health, defenses, food and experience.
 * Nullable fields are unknown when absent, not zero or unchanged from a previous event.
 */
public final class PlayerVitalsEvent extends Event {
  @SerializedName("health")
  private float health;
  @SerializedName("foodLevel")
  private int foodLevel;
  @SerializedName("saturation")
  private float saturation;
  @SerializedName("xpProgress")
  private float xpProgress;
  @SerializedName("xpLevel")
  private int xpLevel;
  @SerializedName("totalXp")
  private int totalXp;
  @SerializedName("maxHealth")
  private Double maxHealth;
  @SerializedName("absorption")
  private Double absorption;
  @SerializedName("exhaustion")
  private Float exhaustion;
  @SerializedName("armor")
  private Double armor;
  @SerializedName("armorToughness")
  private Double armorToughness;

  public PlayerVitalsEvent() {
  }

  /** All values describe the current state, rather than changes since the last event. */
  public PlayerVitalsEvent(
    float health, int foodLevel, float saturation,
    float xpProgress, int xpLevel, int totalXp
  ) {
    this(health, foodLevel, saturation, xpProgress, xpLevel, totalXp, null, null, null, null, null);
  }

  /**
   * All values describe the current state. Pass null for unavailable attributes;
   * max health, armor and armor toughness are effective values including modifiers.
   */
  public PlayerVitalsEvent(
    float health, int foodLevel, float saturation,
    float xpProgress, int xpLevel, int totalXp,
    Double maxHealth, Double absorption, Float exhaustion,
    Double armor, Double armorToughness
  ) {
    this.health = health;
    this.foodLevel = foodLevel;
    this.saturation = saturation;
    this.xpProgress = xpProgress;
    this.xpLevel = xpLevel;
    this.totalXp = totalXp;
    this.maxHealth = maxHealth;
    this.absorption = absorption;
    this.exhaustion = exhaustion;
    this.armor = armor;
    this.armorToughness = armorToughness;
  }

  /** Health in health points, not hearts. */
  public float health() {
    return health;
  }

  public int foodLevel() {
    return foodLevel;
  }

  public float saturation() {
    return saturation;
  }

  /** Fractional progress toward the next experience level, from 0 to 1. */
  public float xpProgress() {
    return xpProgress;
  }

  public int xpLevel() {
    return xpLevel;
  }

  /** Total experience points reported for the player, not a value derived from the level. */
  public int totalXp() {
    return totalXp;
  }

  /** Effective maximum health in health points, excluding absorption; null when unknown. */
  public Double maxHealth() {
    return maxHealth;
  }

  /** Current absorption in health points, not hearts; null when unknown. */
  public Double absorption() {
    return absorption;
  }

  /** Current food exhaustion; null when unknown. */
  public Float exhaustion() {
    return exhaustion;
  }

  /** Effective armor points, not equipment durability; null when unknown. */
  public Double armor() {
    return armor;
  }

  /** Effective armor toughness; null when unknown. */
  public Double armorToughness() {
    return armorToughness;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
