package ac.intave.samples.event;

import ac.intave.samples.share.*;
import com.google.gson.annotations.SerializedName;

public final class BlockPlaceEvent extends Event {
  @SerializedName("placedBlock")
  private Position placedBlock;
  @SerializedName("againstBlock")
  private Position againstBlock;
  @SerializedName("direction")
  private Direction direction;

  @SerializedName("rotation")
  private Rotation rotation;

  // not always correct due to flying packets
  @SerializedName("eyePosition")
  private Position eyePosition;
  @SerializedName("endOfRaytrace")
  private Position endOfRaytrace;

  // not always correct, can also be 0.0 or NaN
  @SerializedName("facing")
  private Vector3d facing = Vector3d.ZERO;

  @SerializedName("hand")
  private Hand hand;
  @SerializedName("typeName")
  private String typeName;
  @SerializedName("amountInHand")
  private int amountInHand;

  public BlockPlaceEvent() {
  }

  public BlockPlaceEvent(
    Position placedBlock, Position againstBlock,
    Direction direction,
    Rotation rotation,
    Position eyePosition, Position endOfRaytrace,
    Hand hand, String typeName, int amountInHand,
    float facingX, float facingY, float facingZ
  ) {
    this(
      placedBlock, againstBlock, direction, rotation, eyePosition, endOfRaytrace,
      hand, typeName, amountInHand, new Vector3d(facingX, facingY, facingZ)
    );
  }

  public BlockPlaceEvent(
    Position placedBlock,
    Position againstBlock,
    Direction direction,
    Rotation rotation,
    Position eyePosition,
    Position endOfRaytrace,
    Hand hand,
    String typeName,
    int amountInHand,
    Vector3d facing
  ) {
    this.placedBlock = placedBlock;
    this.againstBlock = againstBlock;
    this.direction = direction;
    this.rotation = rotation;
    this.eyePosition = eyePosition;
    this.endOfRaytrace = endOfRaytrace;
    this.hand = hand;
    this.typeName = typeName;
    this.amountInHand = amountInHand;
    this.facing = facing;
  }

  public Position placedBlock() {
    return placedBlock;
  }

  public Position againstBlock() {
    return againstBlock;
  }

  public Direction direction() {
    return direction;
  }

  public Rotation rotation() {
    return rotation;
  }

  public Position eyePosition() {
    return eyePosition;
  }

  public Position endOfRaytrace() {
    return endOfRaytrace;
  }

  public Hand hand() {
    return hand;
  }

  public String typeName() {
    return typeName;
  }

  public int amountInHand() {
    return amountInHand;
  }

  public Vector3d facing() {
    return facing;
  }

  public float facingX() {
    return (float) facing.x();
  }

  public float facingY() {
    return (float) facing.y();
  }

  public float facingZ() {
    return (float) facing.z();
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public static BlockPlaceEvent create(
    Position placedBlock,
    Position againstBlock,
    Direction direction,
    Rotation rotation,
    Position eyePosition,
    Position endOfRaytrace,
    Hand hand,
    String typeName,
    int amountInHand,
    Vector3d facing
  ) {
    return new BlockPlaceEvent(
      placedBlock,
      againstBlock,
      direction,
      rotation,
      eyePosition,
      endOfRaytrace,
      hand,
      typeName,
      amountInHand,
      facing
    );
  }

  public static BlockPlaceEvent create(
    Position placedBlock,
    Position againstBlock,
    Direction direction,
    Rotation rotation,
    Position eyePosition,
    Position endOfRaytrace,
    Hand hand,
    String typeName,
    int amountInHand,
    float facingX,
    float facingY,
    float facingZ
  ) {
    return new BlockPlaceEvent(
      placedBlock,
      againstBlock,
      direction,
      rotation,
      eyePosition,
      endOfRaytrace,
      hand,
      typeName,
      amountInHand,
      facingX,
      facingY,
      facingZ
    );
  }
}
