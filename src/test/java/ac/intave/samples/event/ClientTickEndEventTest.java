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

import ac.intave.samples.serial.JsonReader;
import ac.intave.samples.serial.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

final class ClientTickEndEventTest {
	@Test
	void registersClientTickEndEvent() {
		EventType<? extends Event> type = EventRegistry.typeNamed("client.tick_end");

		assertEquals(ClientTickEndEvent.class, type.eventClass());
		assertEquals("client.tick_end", EventRegistry.typeOf(new ClientTickEndEvent()).name());
	}

	@Test
	void roundTripsThroughJson() throws Exception {
		ClientTickEndEvent event = new ClientTickEndEvent();
		event.withOffset(42);
		StringWriter output = new StringWriter();
		new JsonWriter(output).visitAny(event);

		try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
			ClientTickEndEvent decoded = assertInstanceOf(ClientTickEndEvent.class, reader.nextEvent());
			assertEquals(42, decoded.offset());
		}
	}

	@Test
	void dispatchesToSpecificSinkVisitor() {
		ClientTickEndEvent event = new ClientTickEndEvent();
		ClientTickEndEvent[] visited = new ClientTickEndEvent[1];
		EventSink sink = new EventSink() {
			@Override
			public void visit(ClientTickEndEvent received) {
				visited[0] = received;
			}

			@Override
			public String name() {
				return "test";
			}
		};

		event.accept(sink);

		assertSame(event, visited[0]);
	}
}
