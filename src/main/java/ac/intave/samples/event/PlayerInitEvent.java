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

import java.util.UUID;

public final class PlayerInitEvent extends Event {
  @SerializedName("name")
  private String name;
  @SerializedName("uuid")
  private UUID uuid;
  @SerializedName("id")
  private int id;
  @SerializedName("clientVersion")
  private int clientVersion;
  @SerializedName("serverVersion")
  private int serverVersion;
  @SerializedName("position")
  private Position position;
  @SerializedName("rotation")
  private Rotation rotation;
  @SerializedName("isFlying")
  private Boolean isFlying;

  public PlayerInitEvent() {
  }

  public PlayerInitEvent(
    int id, int clientVersion, int serverVersion,
    Position position, Rotation rotation
  ) {
    this(null, null, id, clientVersion, serverVersion, position, rotation);
  }

  public PlayerInitEvent(
    String name, UUID uuid,
    int id, int clientVersion, int serverVersion,
    Position position, Rotation rotation
  ) {
    this(name, uuid, id, clientVersion, serverVersion, position, rotation, null);
  }

  public PlayerInitEvent(
    int id, int clientVersion, int serverVersion,
    Position position, Rotation rotation, Boolean isFlying
  ) {
    this(null, null, id, clientVersion, serverVersion, position, rotation, isFlying);
  }

  public PlayerInitEvent(
    String name, UUID uuid,
    int id, int clientVersion, int serverVersion,
    Position position, Rotation rotation, Boolean isFlying
  ) {
    this.name = name;
    this.uuid = uuid;
    this.id = id;
    this.clientVersion = clientVersion;
    this.serverVersion = serverVersion;
    this.position = position;
    this.rotation = rotation;
    this.isFlying = isFlying;
  }

  public String name() {
    return name;
  }

  public UUID uuid() {
    return uuid;
  }

  public int id() {
    return id;
  }

  public int clientVersion() {
    return clientVersion;
  }

  public int serverVersion() {
    return serverVersion;
  }

  public Position position() {
    return position;
  }

  public Rotation rotation() {
    return rotation;
  }

  /** Initial flight state, distinct from permission to fly or gliding; null when unknown. */
  public Boolean isFlying() {
    return isFlying;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
