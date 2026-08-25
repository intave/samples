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

public final class InventoryCloseEvent extends Event {
  @SerializedName("containerId")
  private int containerId;
  @SerializedName("source")
  private Source source = Source.INFERRED;

  public InventoryCloseEvent() {
  }

  public InventoryCloseEvent(int containerId, Source source) {
    this.containerId = containerId;
    this.source = Objects.requireNonNull(source, "source");
  }

  public int containerId() {
    return containerId;
  }

  public Source source() {
    return source;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public enum Source {
    CLIENT,
    SERVER,
    LIFECYCLE,
    INFERRED
  }
}
