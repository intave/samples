/*
 * Copyright 2026 Intave
 *
 * This software is licensed under the PolyForm Perimeter License 1.0.0.
 * You may use this software for any purpose, except for providing to
 * others any product that competes with the software.
 *
 * A copy of the license is available at:
 *   https://polyformproject.org/licenses/perimeter/1.0.0/
 */

package ac.intave.samples.event;

import ac.intave.samples.share.Item;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Deprecated
public final class WindowActionEvent extends Event {
  @SerializedName("windowId")
  private int windowId;
  @SerializedName("action")
  private Action action;
  @SerializedName("armorContents")
  private Item[] armorContents;

  public WindowActionEvent() {
  }

  public WindowActionEvent(int windowId, Action action, Item[] armorContents) {
    this.windowId = windowId;
    this.action = action;
    if (armorContents == null) {
      armorContents = new Item[4];
    }
    if (armorContents.length != 4) {
      throw new IllegalArgumentException("armorContents.length != 4");
    }
    this.armorContents = armorContents.clone();
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public static WindowActionEvent create(
    Action action, Item[] armorContents
  ) {
    return new WindowActionEvent(0, action, armorContents);
  }

  public int windowId() {
    return windowId;
  }

  public Action action() {
    return action;
  }

  public List<Item> armorContents() {
    if (armorContents == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(Arrays.asList(armorContents.clone()));
  }

  public enum Action {
    OPEN,
    INFER_OPEN,
    CLOSE
  }
}
