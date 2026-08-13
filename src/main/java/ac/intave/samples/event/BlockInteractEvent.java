package ac.intave.samples.event;

import ac.intave.samples.share.Hand;
import ac.intave.samples.share.Position;
import ac.intave.samples.share.Rotation;
import com.google.gson.annotations.SerializedName;

public final class BlockInteractEvent extends Event {
  @SerializedName("blockPosition")
  private Position blockPosition;
  @SerializedName("rotation")
  private Rotation rotation;
  @SerializedName("failedBlockPlacement")
  private boolean failedBlockPlacement;

  @SerializedName("hand")
  private Hand hand;
  @SerializedName("blockType")
  private String blockType;
  @SerializedName("itemInHand")
  private String itemInHand;
  @SerializedName("amountInHand")
  private int amountInHand;

  public BlockInteractEvent() {
  }

  public BlockInteractEvent(
    Position blockPosition,
    Rotation rotation,
    boolean failedBlockPlacement,
    Hand hand,
    String blockType,
    String itemInHand,
    int amountInHand
  ) {
    this.blockPosition = blockPosition;
    this.rotation = rotation;
    this.failedBlockPlacement = failedBlockPlacement;
    this.hand = hand;
    this.blockType = blockType;
    this.itemInHand = itemInHand;
    this.amountInHand = amountInHand;
  }

  public Position blockPosition() {
    return blockPosition;
  }

  public Rotation rotation() {
    return rotation;
  }

  public boolean failedBlockPlacement() {
    return failedBlockPlacement;
  }

  public Hand hand() {
    return hand;
  }

  public String blockType() {
    return blockType;
  }

  public String itemInHand() {
    return itemInHand;
  }

  public int amountInHand() {
    return amountInHand;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public static BlockInteractEvent create(
    Position blockPosition, Rotation rotation, boolean failedBlockPlacement,
    Hand hand, String blockType, String itemInHand, int amountInHand
  ) {
    return new BlockInteractEvent(blockPosition, rotation, failedBlockPlacement, hand, blockType, itemInHand, amountInHand);
  }
}
