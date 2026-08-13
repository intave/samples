package ac.intave.samples.event;

import com.google.gson.annotations.SerializedName;

public final class SlotSwitchEvent extends Event {
  @SerializedName("slot")
  private int slot;
  @SerializedName("material")
  private String material;
  @SerializedName("amount")
  private int amount;

  public SlotSwitchEvent() {
  }

  public SlotSwitchEvent(int slot, String material, int amount) {
    this.slot = slot;
    this.material = material;
    this.amount = amount;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public int slot() {
    return slot;
  }

  public String material() {
    return material;
  }

  public int amount() {
    return amount;
  }

  public static SlotSwitchEvent create(int slot, String type, int amount) {
    return new SlotSwitchEvent(slot, type, amount);
  }
}
