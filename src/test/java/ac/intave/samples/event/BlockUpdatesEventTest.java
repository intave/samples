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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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
		Block north = new Block("OAK_STAIRS", Map.of("facing", "north"), Collections.emptyList());
		Block south = new Block("OAK_STAIRS", Map.of("facing", "south"), Collections.emptyList());

		assertNotEquals(north, south);
		assertNotEquals(north.hashCode(), south.hashCode());
	}
}
