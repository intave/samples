package ac.intave.samples.serial;

import ac.intave.samples.event.ClickEvent;
import ac.intave.samples.event.Event;
import ac.intave.samples.event.PlayerMoveEvent;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JsonWriterCompressionTest {
	@Test
	void compressesAtLevelFifteenAndFlushesEveryTwelveHundredMovements() throws IOException {
		FlushCountingOutputStream compressed = new FlushCountingOutputStream();
		try (JsonWriter writer = new JsonWriter(compressed)) {
			for (int i = 1; i < JsonWriter.MOVEMENT_EVENTS_PER_FLUSH; i++) {
				writer.visitAny(new PlayerMoveEvent());
			}
			assertEquals(0, compressed.flushCount);
			writer.visitAny(ClickEvent.create());
			assertEquals(0, compressed.flushCount, "non-movement events must not advance the batch");

			int sizeBeforeBatchFlush = compressed.size();
			writer.visitAny(new PlayerMoveEvent());
			assertEquals(1, compressed.flushCount);
			assertTrue(
				compressed.size() > sizeBeforeBatchFlush,
				"batch flush should emit pending compressed data"
			);
		}

		byte[] bytes = compressed.toByteArray();
		assertEquals(0x28, bytes[0] & 0xff);
		assertEquals(0xb5, bytes[1] & 0xff);
		assertEquals(0x2f, bytes[2] & 0xff);
		assertEquals(0xfd, bytes[3] & 0xff);

		int movementEvents = 0;
		int clickEvents = 0;
		try (JsonReader reader = new JsonReader(new ByteArrayInputStream(bytes))) {
			Event event;
			while ((event = reader.nextEvent()) != null) {
				if (event instanceof PlayerMoveEvent) {
					movementEvents++;
				} else {
					assertInstanceOf(ClickEvent.class, event);
					clickEvents++;
				}
			}
			assertNull(reader.nextEvent());
		}
		assertEquals(JsonWriter.MOVEMENT_EVENTS_PER_FLUSH, movementEvents);
		assertEquals(1, clickEvents);
	}

	private static final class FlushCountingOutputStream extends ByteArrayOutputStream {
		private int flushCount;

		@Override
		public void flush() throws IOException {
			flushCount++;
			super.flush();
		}
	}
}
