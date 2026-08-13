package ac.intave.samples.event;

import com.google.gson.annotations.SerializedName;

public final class WindowClickEvent extends Event {
  @SerializedName("windowId")
  private int windowId;
  @SerializedName("slot")
  private int slot;
  @SerializedName("button")
  private int button;
  @SerializedName("action")
  private int action;
  @SerializedName("mode")
  private int mode;
  @SerializedName("unused")
  private String unused = "";
  @SerializedName("connectionStability")
  private int connectionStability;
  @SerializedName("timestamp")
  private long timestamp;

  public WindowClickEvent() {
  }

  public WindowClickEvent(
    int windowId, int slot,
    int button, int action,
    int mode,
    String unused, int connectionStability,
    long timestamp
  ) {
    this.windowId = windowId;
    this.slot = slot;
    this.button = button;
    this.action = action;
    this.mode = mode;
    this.unused = unused;
    this.connectionStability = connectionStability;
    this.timestamp = timestamp;
  }

  public static WindowClickEvent create(
    int container, int slot, int clickType, int button, int mode
  ) {
    return new WindowClickEvent(container, slot, button, clickType, mode, "null", 0, System.currentTimeMillis());
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public int windowId() {
    return windowId;
  }

  public int slot() {
    return slot;
  }

  public int button() {
    return button;
  }

  public int action() {
    return action;
  }

  public int mode() {
    return mode;
  }

  public String unused() {
    return unused;
  }

  public int connectionStability() {
    return connectionStability;
  }

  public long timestamp() {
    return timestamp;
  }
}
