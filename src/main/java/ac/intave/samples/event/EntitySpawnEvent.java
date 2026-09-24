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

import ac.intave.samples.share.HitboxSize;
import ac.intave.samples.share.Position;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public final class EntitySpawnEvent extends Event {
  @SerializedName("id")
  private int id;
  @SerializedName("name")
  private String name;
  @SerializedName("size")
  private HitboxSize size;
  @SerializedName("position")
  private Position position;
  @SerializedName("uuid")
  private String uuid;
  @SerializedName("playerName")
  private String playerName;

  public EntitySpawnEvent() {
  }

  public EntitySpawnEvent(
    UUID uuid,
    int id, String name,
    HitboxSize size,
    String playerName,
    Position position
  ) {
    this.uuid = uuid == null ? null : uuid.toString();
    this.id = id;
    this.name = name;
    this.size = size;
    this.playerName = playerName;
    this.position = position;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public int id() {
    return id;
  }

  public String name() {
    return name;
  }

  public HitboxSize size() {
    return size;
  }

  public Position position() {
    return position;
  }

  public String uuid() {
    return uuid;
  }

  public String playerName() {
    return playerName;
  }
}
