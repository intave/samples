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

import java.util.Objects;

public final class EventType<T extends Event> {
  private final String name;
  private final Class<T> eventClass;

  EventType(
    String name,
    Class<T> eventClass
  ) {
    this.name = Objects.requireNonNull(name, "name");
    this.eventClass = Objects.requireNonNull(eventClass, "eventClass");
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Event type name cannot be empty");
    }
  }

  public String name() {
    return name;
  }

  public Class<T> eventClass() {
    return eventClass;
  }
}
