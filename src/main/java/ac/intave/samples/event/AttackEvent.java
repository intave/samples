package ac.intave.samples.event;

import com.google.gson.annotations.SerializedName;

public final class AttackEvent extends Event {
  @SerializedName("source")
  private int source;
  @SerializedName("target")
  private int target;

  public AttackEvent() {
  }

  public AttackEvent(int source, int target) {
    this.source = source;
    this.target = target;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public int source() {
    return source;
  }

  public int target() {
    return target;
  }

  public static AttackEvent create(int source, int target) {
    return new AttackEvent(source, target);
  }
}
