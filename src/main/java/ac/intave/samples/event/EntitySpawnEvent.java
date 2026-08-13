package ac.intave.samples.event;

import ac.intave.samples.share.HitboxSize;
import ac.intave.samples.share.Position;
import com.google.gson.annotations.SerializedName;

public final class EntitySpawnEvent extends Event {
  @SerializedName("id")
  private int id;
  @SerializedName("name")
  private String name;
  @SerializedName("size")
  private HitboxSize size;
  @SerializedName("position")
  private Position position;

  public EntitySpawnEvent() {
  }

  public EntitySpawnEvent(
    int id, String name,
    HitboxSize size, Position position
  ) {
    this.id = id;
    this.name = name;
    this.size = size;
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
}
