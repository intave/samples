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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class MarkerEventTest {
	@Test
	void registersMarkerEvent() {
		EventType<? extends Event> type = EventRegistry.typeNamed("marker");

		assertEquals(MarkerEvent.class, type.eventClass());
		assertEquals("marker", EventRegistry.typeOf(new MarkerEvent()).name());
	}

	@Test
	void roundTripsUuidThroughJson() throws Exception {
		UUID uuid = UUID.fromString("c577faa5-cb4b-4921-ad58-6191853bb16d");
		MarkerEvent event = new MarkerEvent(uuid);
		event.withOffset(42);
		StringWriter output = new StringWriter();
		new JsonWriter(output).visitAny(event);

		try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
			MarkerEvent decoded = assertInstanceOf(MarkerEvent.class, reader.nextEvent());
			assertEquals(uuid, decoded.uuid());
			assertEquals(42, decoded.offset());
		}
	}

	@Test
	void dispatchesToSpecificSinkVisitor() {
		MarkerEvent event = new MarkerEvent(UUID.randomUUID());
		MarkerEvent[] visited = new MarkerEvent[1];
		EventSink sink = new EventSink() {
			@Override
			public void visit(MarkerEvent received) {
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
