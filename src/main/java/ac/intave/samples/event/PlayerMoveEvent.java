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

import java.util.Locale;
import java.util.Objects;

public final class PlayerMoveEvent extends Event {
  @SerializedName("keys")
  private KeyCombination keys = KeyCombination.NONE;
  @SerializedName("position")
  private Position position = Position.ZERO;
  @SerializedName("rotation")
  private Rotation rotation = Rotation.ZERO;
  @SerializedName("collidedHorizontally")
  private boolean collidedHorizontally;
  @SerializedName("collidedVertically")
  private boolean collidedVertically;
  @SerializedName("inWater")
  private boolean inWater;
  @SerializedName("inLava")
  private boolean inLava;
  @SerializedName("inVehicle")
  private boolean inVehicle;
  @SerializedName("sneaking")
  private boolean sneaking;
  @SerializedName("clientSneaking")
  private Boolean clientSneaking;
  @SerializedName("recentlyTeleported")
  private boolean recentlyTeleported;
  @SerializedName("jumped")
  private boolean jumped;
  @SerializedName("sprinting")
  private Boolean sprinting;
  @SerializedName("pose")
  private String pose;

  public PlayerMoveEvent() {
  }

  /** Complete movement snapshot. Nullable fields are unknown when not recorded. */
  public PlayerMoveEvent(
    Position position, Rotation rotation, int keyOrdinal,
    boolean collidedHorizontally, boolean collidedVertically,
    boolean inWater, boolean inLava, boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped, Boolean sprinting, Boolean clientSneaking, String pose
  ) {
    this.keys = KeyCombination.values()[keyOrdinal];
    this.position = position;
    this.rotation = rotation;
    this.collidedHorizontally = collidedHorizontally;
    this.collidedVertically = collidedVertically;
    this.inWater = inWater;
    this.inLava = inLava;
    this.inVehicle = inVehicle;
    this.sneaking = sneaking;
    this.recentlyTeleported = recentlyTeleported;
    this.jumped = jumped;
    this.sprinting = sprinting;
    this.clientSneaking = clientSneaking;
    this.pose = pose;
  }

  public Position position() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public double x() {
    return position.x();
  }

  public void setX(double x) {
    position = new Position(x, position.y(), position.z());
  }

  public double y() {
    return position.y();
  }

  public void setY(double y) {
    position = new Position(position.x(), y, position.z());
  }

  public double z() {
    return position.z();
  }

  public void setZ(double z) {
    position = new Position(position.x(), position.y(), z);
  }

  public Rotation rotation() {
    return rotation;
  }

  public void setRotation(Rotation rotation) {
    this.rotation = rotation;
  }

  public float yaw() {
    return rotation.yaw();
  }

  public void setYaw(float yaw) {
    rotation = new Rotation(yaw, rotation.pitch());
  }

  public float pitch() {
    return rotation.pitch();
  }

  public void setPitch(float pitch) {
    rotation = new Rotation(rotation.yaw(), pitch);
  }

  public boolean collidedHorizontally() {
    return collidedHorizontally;
  }

  public boolean collidedVertically() {
    return collidedVertically;
  }

  public boolean inWater() {
    return inWater;
  }

  public boolean inLava() {
    return inLava;
  }

  public boolean inVehicle() {
    return inVehicle;
  }

  public boolean sneaking() {
    return sneaking;
  }

  /** Client-reported sneak state at this snapshot; null when unknown, independent of sneaking(). */
  public Boolean clientSneaking() {
    return clientSneaking;
  }

  public boolean recentlyTeleported() {
    return recentlyTeleported;
  }

  public boolean jumped() {
    return jumped;
  }

  /** Sprint state at this movement snapshot; null when unknown or absent in older recordings. */
  public Boolean sprinting() {
    return sprinting;
  }

  /**
   * Pose name at this snapshot, such as STANDING, CROUCHING, SWIMMING or FALL_FLYING.
   * Null means unknown. Names are preserved verbatim to support new poses without
   * a protocol enum update; producers should use the pose's enum name, not its ordinal.
   */
  public String pose() {
    return pose;
  }

  public String input() {
    return keys.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  @Override
  public String toString() {
    return "PlayerMoveEvent{" +
      "keys=" + keys +
      ", position=" + position +
      ", rotation=" + rotation +
      ", collidedHorizontally=" + collidedHorizontally +
      ", collidedVertically=" + collidedVertically +
      ", inWater=" + inWater +
      ", inLava=" + inLava +
      ", inVehicle=" + inVehicle +
      ", sneaking=" + sneaking +
      ", clientSneaking=" + clientSneaking +
      ", recentlyTeleported=" + recentlyTeleported +
      ", jumped=" + jumped +
      ", sprinting=" + sprinting +
      ", pose=" + pose +
      '}';
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + keys.hashCode();
    result = 31 * result + position.hashCode();
    result = 31 * result + rotation.hashCode();
    result = 31 * result + Boolean.hashCode(collidedHorizontally);
    result = 31 * result + Boolean.hashCode(collidedVertically);
    result = 31 * result + Boolean.hashCode(inWater);
    result = 31 * result + Boolean.hashCode(inLava);
    result = 31 * result + Boolean.hashCode(inVehicle);
    result = 31 * result + Boolean.hashCode(sneaking);
    result = 31 * result + Boolean.hashCode(recentlyTeleported);
    result = 31 * result + Boolean.hashCode(jumped);
    result = 31 * result + Objects.hashCode(sprinting);
    result = 31 * result + Objects.hashCode(clientSneaking);
    result = 31 * result + Objects.hashCode(pose);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PlayerMoveEvent)) return false;
    PlayerMoveEvent other = (PlayerMoveEvent) obj;
    return keys == other.keys &&
      position.equals(other.position) &&
      rotation.equals(other.rotation) &&
      collidedHorizontally == other.collidedHorizontally &&
      collidedVertically == other.collidedVertically &&
      inWater == other.inWater &&
      inLava == other.inLava &&
      inVehicle == other.inVehicle &&
      sneaking == other.sneaking &&
      recentlyTeleported == other.recentlyTeleported &&
      jumped == other.jumped &&
      Objects.equals(sprinting, other.sprinting) &&
      Objects.equals(clientSneaking, other.clientSneaking) &&
      Objects.equals(pose, other.pose);
  }

  public static PlayerMoveEvent create(
    float strafe, float forward,
    Position position, Rotation rotation,
    boolean collidedHorizontally,
    boolean collidedVertically,
    boolean inWater,
    boolean inLava,
    boolean inVehicle,
    boolean sneaking,
    boolean recentlyTeleported,
    boolean jumped
  ) {
    return new PlayerMoveEvent(
      position, rotation, KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped, null, null, null
    );
  }

  public static PlayerMoveEvent create(
    float strafe, float forward,
    double x, double y, double z,
    float yaw, float pitch,
    boolean collidedHorizontally,
    boolean collidedVertically,
    boolean inWater,
    boolean inLava,
    boolean inVehicle,
    boolean sneaking,
    boolean recentlyTeleported,
    boolean jumped
  ) {
    return new PlayerMoveEvent(
      new Position(x, y, z), new Rotation(yaw, pitch), KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped, null, null, null
    );
  }

  public static PlayerMoveEvent create(
    float strafe, float forward, Position position, Rotation rotation,
    boolean collidedHorizontally, boolean collidedVertically,
    boolean inWater, boolean inLava, boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped, Boolean sprinting
  ) {
    return new PlayerMoveEvent(position, rotation, KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped, sprinting, null, null);
  }

  public static PlayerMoveEvent create(
    float strafe, float forward,
    double x, double y, double z, float yaw, float pitch,
    boolean collidedHorizontally, boolean collidedVertically,
    boolean inWater, boolean inLava, boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped, Boolean sprinting
  ) {
    return new PlayerMoveEvent(new Position(x, y, z), new Rotation(yaw, pitch), KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped, sprinting, null, null);
  }

  public static PlayerMoveEvent create(
    float strafe, float forward, Position position, Rotation rotation,
    boolean collidedHorizontally, boolean collidedVertically,
    boolean inWater, boolean inLava, boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped, Boolean sprinting, Boolean clientSneaking
  ) {
    return new PlayerMoveEvent(position, rotation, KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped, sprinting, clientSneaking, null);
  }

  public static PlayerMoveEvent create(
    float strafe, float forward,
    double x, double y, double z, float yaw, float pitch,
    boolean collidedHorizontally, boolean collidedVertically,
    boolean inWater, boolean inLava, boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped, Boolean sprinting, Boolean clientSneaking
  ) {
    return new PlayerMoveEvent(new Position(x, y, z), new Rotation(yaw, pitch), KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped, sprinting, clientSneaking, null);
  }

  public static PlayerMoveEvent create(
    float strafe, float forward, Position position, Rotation rotation,
    boolean collidedHorizontally, boolean collidedVertically,
    boolean inWater, boolean inLava, boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped, Boolean sprinting, Boolean clientSneaking, String pose
  ) {
    return new PlayerMoveEvent(position, rotation, KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped, sprinting, clientSneaking, pose);
  }

  public static PlayerMoveEvent create(
    float strafe, float forward,
    double x, double y, double z, float yaw, float pitch,
    boolean collidedHorizontally, boolean collidedVertically,
    boolean inWater, boolean inLava, boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped, Boolean sprinting, Boolean clientSneaking, String pose
  ) {
    return new PlayerMoveEvent(new Position(x, y, z), new Rotation(yaw, pitch), KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped, sprinting, clientSneaking, pose);
  }

  private enum KeyCombination {
    NONE,
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    FORWARD_LEFT,
    FORWARD_RIGHT,
    BACKWARD_LEFT,
    BACKWARD_RIGHT;

    public static KeyCombination from(float strafe, float forward) {
      if (forward > 0) {
        if (strafe > 0) {
          return FORWARD_RIGHT;
        } else if (strafe < 0) {
          return FORWARD_LEFT;
        } else {
          return FORWARD;
        }
      } else if (forward < 0) {
        if (strafe > 0) {
          return BACKWARD_RIGHT;
        } else if (strafe < 0) {
          return BACKWARD_LEFT;
        } else {
          return BACKWARD;
        }
      } else {
        if (strafe > 0) {
          return RIGHT;
        } else if (strafe < 0) {
          return LEFT;
        } else {
          return NONE;
        }
      }
    }
  }
}
