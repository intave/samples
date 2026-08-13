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
    this.name = name;
    this.uuid = uuid;
    this.id = id;
    this.clientVersion = clientVersion;
    this.serverVersion = serverVersion;
    this.position = position;
    this.rotation = rotation;
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

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
