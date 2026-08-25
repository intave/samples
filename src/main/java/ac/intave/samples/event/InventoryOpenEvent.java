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

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public final class InventoryOpenEvent extends Event {
  @SerializedName("containerId")
  private int containerId;
  @SerializedName("menuType")
  private String menuType = "unknown";
  @SerializedName("inferred")
  private boolean inferred;

  public InventoryOpenEvent() {
  }

  public InventoryOpenEvent(int containerId, String menuType, boolean inferred) {
    this.containerId = containerId;
    this.menuType = Objects.requireNonNull(menuType, "menuType");
    this.inferred = inferred;
  }

  public int containerId() {
    return containerId;
  }

  public String menuType() {
    return menuType;
  }

  public boolean inferred() {
    return inferred;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
