package ac.intave.samples.serial;

import ac.intave.samples.event.*;
import ac.intave.samples.share.Classifier;
import ac.intave.samples.share.Direction;
import ac.intave.samples.share.Hand;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LegacyBinaryDeserializerTest {
	private static final String LEGACY_SAMPLE = "a1398896-3de1-4427-bebd-9df51d983528";

	@Test
	void deserializesBundledLegacyRecording() throws IOException {
		try (
			InputStream stream = Objects.requireNonNull(
				getClass().getResourceAsStream(
					"/" + LEGACY_SAMPLE + ".sample"
				)
			)
		) {
			LegacyBinaryDeserializer deserializer = new LegacyBinaryDeserializer(stream);
			assertInstanceOf(HeaderEvent.class, deserializer.nextEvent());

			int eventCount = 0;
			Event event;
			while ((event = deserializer.nextEvent()) != null) {
				eventCount++;
			}
			assertTrue(eventCount > 1_000, "recording should contain its complete event stream");
			assertNull(deserializer.nextEvent(), "end of stream should be idempotent");
		}
	}

	@Test
	void writesBundledLegacyRecordingAsJsonLines() throws IOException {
		Path output = Paths.get(
			"build", "legacy-samples", LEGACY_SAMPLE + ".jsonl.zst"
		).toAbsolutePath();
		Files.createDirectories(output.getParent());

		int writtenLines = 0;
		try (
			InputStream stream = Objects.requireNonNull(
				getClass().getResourceAsStream("/" + LEGACY_SAMPLE + ".sample")
			);
			JsonWriter writer = new JsonWriter(Files.newOutputStream(output))
		) {
			LegacyBinaryDeserializer deserializer = new LegacyBinaryDeserializer(stream);
			Event event;
			while ((event = deserializer.nextEvent()) != null) {
				event.accept(writer);
				writtenLines++;
			}
		}

		assertTrue(writtenLines > 1_000, "recording should produce its complete JSONL stream");
		int readEvents = 0;
		try (JsonReader reader = new JsonReader(Files.newInputStream(output))) {
			Event event;
			while ((event = reader.nextEvent()) != null) {
				if (readEvents == 0) {
					assertInstanceOf(HeaderEvent.class, event);
				}
				readEvents++;
			}
			assertNull(reader.nextEvent(), "end of JSONL stream should be idempotent");
		}
		assertEquals(writtenLines, readEvents);
	}

	@Test
	void resolvesDeltaCompressedMovesAndZeroSeparators() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(bytes)) {
			writeHeader(out, 0x1001);

			writeEvent(out, 3, 6, event -> {
				event.writeInt(42);
				event.writeInt(47);
				event.writeInt(763);
				writePosition(event, 1, 2, 3);
				event.writeFloat(4);
				event.writeFloat(5);
			});
			writeEvent(out, 7, 2, event -> {
				event.writeByte(0x11); // X and pitch
				event.writeDouble(10);
				event.writeFloat(20);
				event.writeByte(6); // forward-right
				event.writeShort(0x81); // horizontal collision and jump
			});
			writeEvent(out, 11, 7, event -> {
				event.writeInt(7);
				event.writeFloat(0.6f);
				event.writeFloat(1.8f);
				writePosition(event, 30, 40, 50);
			});
			writeEvent(out, 13, 3, event -> {
				event.writeInt(7);
				event.writeInt(0x08); // yaw only
				event.writeFloat(90);
				event.writeBoolean(true);
			});
			out.writeShort(0);
			out.writeByte(-1);
		}

		LegacyBinaryDeserializer deserializer = new LegacyBinaryDeserializer(
			new ByteArrayInputStream(bytes.toByteArray())
		);
		HeaderEvent header = assertInstanceOf(HeaderEvent.class, deserializer.nextEvent());
		assertNotNull(header);
		assertEquals(Classifier.LEGIT, header.classifier());

		PlayerInitEvent init = assertInstanceOf(PlayerInitEvent.class, deserializer.nextEvent());
		assertEquals(42, init.id());
		assertEquals(3, init.offset());

		PlayerMoveEvent playerMove = assertInstanceOf(PlayerMoveEvent.class, deserializer.nextEvent());
		assertEquals(10, playerMove.x());
		assertEquals(2, playerMove.y());
		assertEquals(3, playerMove.z());
		assertEquals(4, playerMove.yaw());
		assertEquals(20, playerMove.pitch());
		assertEquals("forward_right", playerMove.input());
		assertTrue(playerMove.collidedHorizontally());
		assertTrue(playerMove.jumped());
		assertFalse(playerMove.collidedVertically());
		assertFalse(playerMove.inWater());
		assertFalse(playerMove.inLava());
		assertFalse(playerMove.inVehicle());
		assertFalse(playerMove.sneaking());
		assertFalse(playerMove.recentlyTeleported());

		JsonObject playerMoveJson = new Gson().toJsonTree(playerMove).getAsJsonObject();
		assertFalse(playerMoveJson.has("characteristicFlags"));
		assertTrue(playerMoveJson.get("collidedHorizontally").getAsBoolean());
		assertFalse(playerMoveJson.get("collidedVertically").getAsBoolean());
		assertFalse(playerMoveJson.get("inWater").getAsBoolean());
		assertFalse(playerMoveJson.get("inLava").getAsBoolean());
		assertFalse(playerMoveJson.get("inVehicle").getAsBoolean());
		assertFalse(playerMoveJson.get("sneaking").getAsBoolean());
		assertFalse(playerMoveJson.get("recentlyTeleported").getAsBoolean());
		assertTrue(playerMoveJson.get("jumped").getAsBoolean());

		EntitySpawnEvent spawn = assertInstanceOf(EntitySpawnEvent.class, deserializer.nextEvent());
		assertNotNull(spawn);
		assertEquals(30, spawn.position().x());

		EntityMoveEvent entityMove = assertInstanceOf(EntityMoveEvent.class, deserializer.nextEvent());
		assertNotNull(entityMove);
		assertEquals(30, entityMove.x());
		assertEquals(40, entityMove.y());
		assertEquals(50, entityMove.z());
		assertEquals(90, entityMove.yaw());
		assertTrue(entityMove.applyYaw());
		assertFalse(entityMove.applyX());
		assertTrue(entityMove.inSight());
		assertNull(deserializer.nextEvent());
	}

	@Test
	void mapsOriginalDirectionWireOrder() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(bytes)) {
			writeHeader(out, 0x1000);
			writeEvent(out, 1, 9, event -> {
				event.writeInt(1);
				event.writeInt(2);
				event.writeInt(3);
				event.writeBoolean(false);
				event.writeByte(1);
				event.writeFloat(10);
				event.writeFloat(20);
				writePosition(event, 1.5, 2.5, 3.5);
				writePosition(event, 4.5, 5.5, 6.5);
				event.writeByte(0);
				event.writeUTF("STONE");
				event.writeInt(64);
				event.writeFloat(0.1f);
				event.writeFloat(0.2f);
				event.writeFloat(0.3f);
			});
			out.writeShort(0);
			out.writeByte(-1);
		}

		LegacyBinaryDeserializer deserializer = new LegacyBinaryDeserializer(
			new ByteArrayInputStream(bytes.toByteArray())
		);
		deserializer.nextEvent();
		BlockPlaceEvent event = assertInstanceOf(BlockPlaceEvent.class, deserializer.nextEvent());
		assertNotNull(event);
		assertEquals(Direction.UP, event.direction());
		assertEquals(Hand.MAIN_HAND, event.hand());
		assertNull(deserializer.nextEvent());
	}

	private static void writeHeader(DataOutputStream out, int flags) throws IOException {
		out.writeUTF("INTAVE/SAMPLE");
		out.writeUTF("test");
		UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
		out.writeLong(id.getMostSignificantBits());
		out.writeLong(id.getLeastSignificantBits());
		out.writeLong(1234);
		out.writeInt(flags);
	}

	private static void writeEvent(
		DataOutputStream out, int offset, int eventId, EventPayload payload
	) throws IOException {
		out.writeShort(offset);
		out.writeByte(eventId);
		payload.write(out);
		out.writeByte(0);
	}

	private static void writePosition(
		DataOutputStream out, double x, double y, double z
	) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(z);
	}

	@FunctionalInterface
	private interface EventPayload {
		void write(DataOutputStream out) throws IOException;
	}
}
