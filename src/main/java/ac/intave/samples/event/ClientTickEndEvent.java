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

/** Marks the end of a client tick. */
public final class ClientTickEndEvent extends Event {
	public ClientTickEndEvent() {
	}

	@Override
	public void accept(EventSink sink) {
		sink.visit(this);
	}
}
