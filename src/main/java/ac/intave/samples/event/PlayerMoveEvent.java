package ac.intave.samples.event;

import ac.intave.samples.share.Blocks;
import ac.intave.samples.share.Position;
import ac.intave.samples.share.Rotation;
import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public final class PlayerMoveEvent extends Event {
  @SerializedName("keys")
  private KeyCombination keys = KeyCombination.NONE;
  @SerializedName("position")
  private Position position = Position.ZERO;
  @SerializedName("rotation")
  private Rotation rotation = Rotation.ZERO;
  @SerializedName("blocks")
  private Blocks blocks = Blocks.empty();
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
  @SerializedName("recentlyTeleported")
  private boolean recentlyTeleported;
  @SerializedName("jumped")
  private boolean jumped;

  public PlayerMoveEvent() {
  }

  public PlayerMoveEvent(
    float strafe, float forward,
    double x, double y, double z,
    float yaw, float pitch,
    boolean collidedHorizontally,
    boolean collidedVertically,
    boolean inWater, boolean inLava,
    boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped
  ) {
    this(
      strafe, forward,
      new Position(x, y, z), new Rotation(yaw, pitch),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped
    );
  }

  public PlayerMoveEvent(
    float strafe, float forward,
    Position position, Rotation rotation,
    boolean collidedHorizontally,
    boolean collidedVertically,
    boolean inWater, boolean inLava,
    boolean inVehicle, boolean sneaking,
    boolean recentlyTeleported, boolean jumped
  ) {
    this(
      position, rotation, KeyCombination.from(strafe, forward).ordinal(),
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped
    );
  }

  public PlayerMoveEvent(
    Position position, Rotation rotation,
    int keyOrdinal,
    boolean collidedHorizontally,
    boolean collidedVertically,
    boolean inWater,
    boolean inLava,
    boolean inVehicle,
    boolean sneaking,
    boolean recentlyTeleported,
    boolean jumped
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

  public boolean recentlyTeleported() {
    return recentlyTeleported;
  }

  public boolean jumped() {
    return jumped;
  }

  public Blocks blocks() {
    return blocks;
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
      ", recentlyTeleported=" + recentlyTeleported +
      ", jumped=" + jumped +
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
      jumped == other.jumped;
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
      strafe, forward,
      position, rotation,
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped
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
      strafe, forward,
      x, y, z, yaw, pitch,
      collidedHorizontally, collidedVertically, inWater, inLava,
      inVehicle, sneaking, recentlyTeleported, jumped
    );
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
