package ac.intave.samples.event;

import com.google.gson.annotations.SerializedName;

public final class EntityRemoveEvent extends Event {
  @SerializedName("id")
  private int id;

  public EntityRemoveEvent() {
  }

  public EntityRemoveEvent(int entityId) {
    this.id = entityId;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public int id() {
    return id;
  }
}
