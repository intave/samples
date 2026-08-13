package ac.intave.samples.event;

import ac.intave.samples.share.Item;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public final class WindowItemsEvent extends Event {
  @SerializedName("windowId")
  private int windowId;
  @SerializedName("count")
  private int count;

  @SerializedName("items")
  private final Map<Integer, Item> items = new HashMap<>();

  public WindowItemsEvent() {
  }

  public WindowItemsEvent(
    int windowId, int count,
    Map<Integer, Item> items
  ) {
    this.windowId = windowId;
    this.count = count;
    this.items.putAll(items);
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public int windowId() {
    return windowId;
  }

  public int count() {
    return count;
  }

  public Map<Integer, Item> items() {
    return items;
  }

  public void addItem(int slot, Item item) {
    items.put(slot, item);
  }

  public void removeItem(int index) {
    items.remove(index);
  }

  public void clearItems() {
    items.clear();
  }

  public static WindowItemsEvent create(
    int windowId, int slots, Map<Integer, Item> items
  ) {
    return new WindowItemsEvent(windowId, slots, items);
  }
}
