package ac.intave.samples.event;

import ac.intave.samples.serial.JsonReader;
import ac.intave.samples.serial.JsonWriter;
import ac.intave.samples.share.Block;
import ac.intave.samples.share.BlockPosition;
import ac.intave.samples.share.BlockUpdate;
import ac.intave.samples.share.BoundingBox;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

final class BlockUpdatesEventTest {
	@Test
	void registersRequestedEventName() {
		EventType<? extends Event> type = EventRegistry.typeNamed("environment.block_updates");

		assertEquals(BlockUpdatesEvent.class, type.eventClass());
		assertEquals("environment.block_updates", EventRegistry.typeOf(new BlockUpdatesEvent()).name());
	}

	@Test
	void roundTripsUpdatesAsAList() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put("facing", "north");
		Block block = new Block(
			"OAK_STAIRS",
			properties,
			Collections.singletonList(new BoundingBox(0, 0, 0, 1, 0.5, 1))
		);
		BlockUpdatesEvent event = new BlockUpdatesEvent(Arrays.asList(
			new BlockUpdate(new BlockPosition(3, 64, -2), block),
			new BlockUpdate(new BlockPosition(4, 64, -2), Block.AIR)
		));
		StringWriter output = new StringWriter();
		new JsonWriter(output).visitAny(event);

		BlockUpdatesEvent decoded;
		try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
			decoded = assertInstanceOf(BlockUpdatesEvent.class, reader.nextEvent());
		}

		assertEquals(event.updates(), decoded.updates());
	}

	@Test
	void splitsLargeUpdatesIntoReadableEvents() throws Exception {
		List<BlockUpdate> updates = updates(24, 40);
		BlockUpdatesEvent event = new BlockUpdatesEvent(updates);
		event.withOffset(42L);
		StringWriter output = new StringWriter();

		new JsonWriter(output).visitAny(event);

		String[] lines = output.toString().split("\\n");
		assertTrue(lines.length > 1);
		for (String line : lines) {
			assertTrue(line.length() <= JsonReader.MAX_EVENT_CHARACTERS);
		}
		List<BlockUpdate> decodedUpdates = new ArrayList<>();
		int decodedEvents = 0;
		try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
			Event decoded;
			while ((decoded = reader.nextEvent()) != null) {
				BlockUpdatesEvent blockUpdates = assertInstanceOf(BlockUpdatesEvent.class, decoded);
				assertEquals(decodedEvents++ == 0 ? 42L : 0L, blockUpdates.offset());
				decodedUpdates.addAll(blockUpdates.updates());
			}
		}
		assertEquals(lines.length, decodedEvents);
		assertEquals(updates, decodedUpdates);
	}

	@Test
	void rejectsOversizedUpdateBeforeWriting() {
		BlockUpdatesEvent event = new BlockUpdatesEvent(updates(1, 400));
		StringWriter output = new StringWriter();
		JsonWriter writer = new JsonWriter(output);

		assertThrows(IllegalArgumentException.class, () -> writer.visitAny(event));
		assertEquals("", output.toString());
	}

	private static List<BlockUpdate> updates(int updateCount, int boxesPerBlock) {
		List<BoundingBox> boxes = new ArrayList<>();
		for (int index = 0; index < boxesPerBlock; index++) {
			double min = index / (double) boxesPerBlock;
			double max = (index + 1) / (double) boxesPerBlock;
			boxes.add(new BoundingBox(min, 0.0, 0.0, max, 1.0, 1.0));
		}
		Block block = new Block(
			"TEST_\"BLOCK",
			Collections.singletonMap("quoted\\key", "<value>"),
			boxes
		);
		List<BlockUpdate> updates = new ArrayList<>();
		for (int index = 0; index < updateCount; index++) {
			updates.add(new BlockUpdate(new BlockPosition(index, 64, -3), block));
		}
		return updates;
	}

	@Test
	void dispatchesToSpecificSinkVisitor() {
		BlockUpdatesEvent event = new BlockUpdatesEvent();
		BlockUpdatesEvent[] visited = new BlockUpdatesEvent[1];
		EventSink sink = new EventSink() {
			@Override
			public void visit(BlockUpdatesEvent received) {
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

	@Test
	void blockIdentityIncludesProperties() {
		Block north = new Block("OAK_STAIRS", Collections.singletonMap("facing", "north"), Collections.emptyList());
		Block south = new Block("OAK_STAIRS", Collections.singletonMap("facing", "south"), Collections.emptyList());

		assertNotEquals(north, south);
		assertNotEquals(north.hashCode(), south.hashCode());
	}
}
