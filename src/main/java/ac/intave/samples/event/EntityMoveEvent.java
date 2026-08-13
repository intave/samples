package ac.intave.samples.event;

import ac.intave.samples.share.Position;
import ac.intave.samples.share.Rotation;
import com.google.gson.annotations.SerializedName;

public final class EntityMoveEvent extends Event {
  @SerializedName("entityId")
  private int entityId;
  @SerializedName("flags")
  private int flags;
  @SerializedName("position")
  private Position position = new Position(0, 0, 0);
  @SerializedName("rotation")
  private Rotation rotation = new Rotation(0, 0);
  @SerializedName("inSight")
  private boolean inSight;

  public EntityMoveEvent() {
  }

  public EntityMoveEvent(int entityId, double x, double y, double z, float yaw, float pitch) {
    this(entityId, new Position(x, y, z), new Rotation(yaw, pitch));
  }

  public EntityMoveEvent(
    int entityId,
    double x, double y, double z,
    float yaw, float pitch, boolean inSight
  ) {
    this(entityId, new Position(x, y, z), new Rotation(yaw, pitch), inSight);
  }

  public EntityMoveEvent(int entityId, Position position, Rotation rotation) {
    this(entityId, position, rotation, false);
  }

  public EntityMoveEvent(
    int entityId, Position position, Rotation rotation, boolean inSight
  ) {
    this.entityId = entityId;
    this.flags = -1;
    this.position = position;
    this.rotation = rotation;
    this.inSight = inSight;
  }

  public EntityMoveEvent(
    int entityId, int flags,
    Position position, Rotation rotation,
    boolean inSight
  ) {
    this.entityId = entityId;
    this.flags = flags;
    this.position = position;
    this.rotation = rotation;
    this.inSight = inSight;
  }

  private static final double EPSILON = 1.0E-09;

  public EntityMoveEvent(
    int entity,
    double x, double y, double z,
    double lastX, double lastY, double lastZ,
    float yaw, float pitch,
    float lastYaw, float lastPitch
  ) {
    this(
      entity,
      new Position(x, y, z), new Position(lastX, lastY, lastZ),
      new Rotation(yaw, pitch), new Rotation(lastYaw, lastPitch)
    );
  }

  public EntityMoveEvent(
    int entity,
    Position position, Position lastPosition,
    Rotation rotation, Rotation lastRotation
  ) {
    this.entityId = entity;
    if (Math.abs(position.x() - lastPosition.x()) > EPSILON) {
      this.flags |= Flag.X;
    }
    if (Math.abs(position.y() - lastPosition.y()) > EPSILON) {
      this.flags |= Flag.Y;
    }
    if (Math.abs(position.z() - lastPosition.z()) > EPSILON) {
      this.flags |= Flag.Z;
    }
    if (Math.abs(rotation.yaw() - lastRotation.yaw()) > EPSILON) {
      this.flags |= Flag.YAW;
    }
    if (Math.abs(rotation.pitch() - lastRotation.pitch()) > EPSILON) {
      this.flags |= Flag.PITCH;
    }
    this.position = position;
    this.rotation = rotation;
  }

  public int entityId() {
    return entityId;
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

  public double y() {
    return position.y();
  }

  public double z() {
    return position.z();
  }

  public void setX(double x) {
    position = new Position(x, position.y(), position.z());
  }

  public void setY(double y) {
    position = new Position(position.x(), y, position.z());
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

  public float pitch() {
    return rotation.pitch();
  }

  public void setYaw(float yaw) {
    rotation = new Rotation(yaw, rotation.pitch());
  }

  public void setPitch(float pitch) {
    rotation = new Rotation(rotation.yaw(), pitch);
  }

  public boolean applyX() {
    return (flags & Flag.X) != 0;
  }

  public boolean applyY() {
    return (flags & Flag.Y) != 0;
  }

  public boolean applyZ() {
    return (flags & Flag.Z) != 0;
  }

  public boolean applyYaw() {
    return (flags & Flag.YAW) != 0;
  }

  public boolean applyPitch() {
    return (flags & Flag.PITCH) != 0;
  }

  public boolean inSight() {
    return inSight;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  @Override
  public String toString() {
    return "(" + entityId + ", " + position + ", " + rotation + ", " + inSight + ")";
  }

  private static class Flag {
    private static final int X = 1;
    private static final int Y = 1 << 1;
    private static final int Z = 1 << 2;
    private static final int YAW = 1 << 3;
    private static final int PITCH = 1 << 4;
  }
}
