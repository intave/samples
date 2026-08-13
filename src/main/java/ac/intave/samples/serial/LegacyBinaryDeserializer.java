package ac.intave.samples.serial;

import ac.intave.samples.event.*;
import ac.intave.samples.share.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LegacyBinaryDeserializer {
	private static final String HEADER = "INTAVE/SAMPLE";
	private static final int MAX_COLLECTION_SIZE = 1024;

	private static final int LINE_FEED_APPEND_FLAG = 1 << 3;
	private static final int ZERO_BYTE_APPEND_FLAG = 1 << 12;
	private static final int UNKNOWN_SEPARATOR = -2;
	private static final int NO_SEPARATOR = -1;
	private static final Direction[] DIRECTION_WIRE_ORDER = {
		Direction.DOWN, Direction.UP, Direction.NORTH,
		Direction.SOUTH, Direction.WEST, Direction.EAST
	};

	private final PushbackInputStream input;
	private final DataInputStream stream;
	private boolean headerRead;
	private boolean finished;
	private int eventSeparator = UNKNOWN_SEPARATOR;

	private int playerId;
	private final Map<Integer, Position> positions = new HashMap<>();
	private final Map<Integer, Rotation> rotations = new HashMap<>();

	public LegacyBinaryDeserializer(InputStream stream) {
		this.input = new PushbackInputStream(stream, 1);
		this.stream = new DataInputStream(input);
	}

	public Event nextEvent() throws IOException {
		if (!headerRead) {
			return readHeader();
		}
		if (finished) {
			return null;
		}

		int duration = stream.readUnsignedShort();
		int eventId = stream.readByte();
		if (duration == 0 && eventId == -1) {
			finished = true;
			return null;
		}

		Event event = readEvent(eventId);
		readEventSeparator();
		event.withOffset(duration);
		return event;
	}

	private HeaderEvent readHeader() throws IOException {
		String header = stream.readUTF();
		if (!HEADER.equals(header)) {
			throw new IOException("Invalid event header: " + header);
		}

		String license = stream.readUTF();
		UUID id = new UUID(stream.readLong(), stream.readLong());
		long createdAt = stream.readLong();
		int flags = stream.readInt();
		boolean lineFeedAppend = (flags & LINE_FEED_APPEND_FLAG) != 0;
		boolean zeroByteAppend = (flags & ZERO_BYTE_APPEND_FLAG) != 0;
		if (lineFeedAppend && zeroByteAppend) {
			throw new IOException("Conflicting event separator flags");
		}
		if (lineFeedAppend) {
			eventSeparator = '\n';
		} else if (zeroByteAppend) {
			eventSeparator = 0;
		}

		headerRead = true;
		return new HeaderEvent(id, license, Classifier.fromFlags(flags), createdAt);
	}

	private Event readEvent(int eventId) throws IOException {
		switch (eventId) {
			case 0:
				return AttackEvent.create(stream.readInt(), stream.readInt());
			case 1:
				return ClickEvent.create();
			case 2:
				return readPlayerMove();
			case 3:
				return readEntityMove();
			case 4:
				return new SlotSwitchEvent(stream.readInt(), stream.readUTF(), stream.readInt());
			case 5:
				return readProperties();
			case 6:
				return readPlayerInit();
			case 7:
				return readEntitySpawn();
			case 8:
				return readEntityRemove();
			case 9:
				return readBlockPlace();
			case 10:
				return readBlockInteract();
			case 11:
				return readWindowClick();
			case 12:
				return readWindowItems();
			case 13:
				return readWindowAction();
			default:
				throw new IOException("Unknown event type: " + eventId);
		}
	}

	private PlayerMoveEvent readPlayerMove() throws IOException {
		int transmissionFlags = stream.readUnsignedByte();
		Position previousPosition = positions.getOrDefault(playerId, Position.ZERO);
		Rotation previousRotation = rotations.getOrDefault(playerId, Rotation.ZERO);
		double x = conditionalReadDouble(stream, transmissionFlags, Flag.X, previousPosition.x());
		double y = conditionalReadDouble(stream, transmissionFlags, Flag.Y, previousPosition.y());
		double z = conditionalReadDouble(stream, transmissionFlags, Flag.Z, previousPosition.z());
		float yaw = conditionalReadFloat(stream, transmissionFlags, Flag.YAW, previousRotation.yaw());
		float pitch = conditionalReadFloat(stream, transmissionFlags, Flag.PITCH, previousRotation.pitch());
		int keys = readOrdinal("key combination", stream.readUnsignedByte(), 9);
		int characteristicFlags = stream.readUnsignedShort();

		Position position = new Position(x, y, z);
		Rotation rotation = new Rotation(yaw, pitch);
		positions.put(playerId, position);
		rotations.put(playerId, rotation);
		return new PlayerMoveEvent(
			position, rotation, keys,
			(characteristicFlags & 1) != 0,
			(characteristicFlags & 2) != 0,
			(characteristicFlags & 4) != 0,
			(characteristicFlags & 8) != 0,
			(characteristicFlags & 16) != 0,
			(characteristicFlags & 32) != 0,
			(characteristicFlags & 64) != 0,
			(characteristicFlags & 128) != 0
		);
	}

	private EntityMoveEvent readEntityMove() throws IOException {
		int entityId = stream.readInt();
		int transmissionFlags = stream.readInt();
		Position previousPosition = positions.getOrDefault(entityId, Position.ZERO);
		Rotation previousRotation = rotations.getOrDefault(entityId, Rotation.ZERO);
		double x = conditionalReadDouble(stream, transmissionFlags, Flag.X, previousPosition.x());
		double y = conditionalReadDouble(stream, transmissionFlags, Flag.Y, previousPosition.y());
		double z = conditionalReadDouble(stream, transmissionFlags, Flag.Z, previousPosition.z());
		float yaw = conditionalReadFloat(stream, transmissionFlags, Flag.YAW, previousRotation.yaw());
		float pitch = conditionalReadFloat(stream, transmissionFlags, Flag.PITCH, previousRotation.pitch());
		boolean inSight = stream.readBoolean();

		Position position = new Position(x, y, z);
		Rotation rotation = new Rotation(yaw, pitch);
		positions.put(entityId, position);
		rotations.put(entityId, rotation);
		return new EntityMoveEvent(entityId, transmissionFlags, position, rotation, inSight);
	}

	private PropertiesEvent readProperties() throws IOException {
		int size = readCollectionSize("properties", stream.readInt());
		Map<String, Boolean> properties = new HashMap<>();
		for (int i = 0; i < size; i++) {
			properties.put(stream.readUTF(), stream.readBoolean());
		}
		return new PropertiesEvent(properties);
	}

	private PlayerInitEvent readPlayerInit() throws IOException {
		int id = stream.readInt();
		int clientVersion = stream.readInt();
		int serverVersion = stream.readInt();
		Position position = readPosition(stream);
		Rotation rotation = readRotation(stream);
		playerId = id;
		positions.put(id, position);
		rotations.put(id, rotation);
		return new PlayerInitEvent(id, clientVersion, serverVersion, position, rotation);
	}

	private EntitySpawnEvent readEntitySpawn() throws IOException {
		int entityId = stream.readInt();
		HitboxSize size = HitboxSize.of(stream.readFloat(), stream.readFloat());
		Position position = readPosition(stream);
		positions.put(entityId, position);
		rotations.put(entityId, Rotation.ZERO);
		return new EntitySpawnEvent(entityId, null, size, position);
	}

	private EntityRemoveEvent readEntityRemove() throws IOException {
		int entityId = stream.readInt();
		positions.remove(entityId);
		rotations.remove(entityId);
		return new EntityRemoveEvent(entityId);
	}

	private BlockPlaceEvent readBlockPlace() throws IOException {
		Position placedBlock = readBlockPosition(stream);
		Position againstBlock = stream.readBoolean() ? readBlockPosition(stream) : null;
		Direction direction = readDirection(stream.readUnsignedByte());
		Rotation rotation = readRotation(stream);
		Position eyePosition = readPosition(stream);
		Position endOfRaytrace = readPosition(stream);
		Hand hand = readEnum("hand", Hand.values(), stream.readUnsignedByte());
		String typeName = stream.readUTF();
		int amountInHand = stream.readInt();
		Vector3d facing = new Vector3d(stream.readFloat(), stream.readFloat(), stream.readFloat());
		return new BlockPlaceEvent(
			placedBlock, againstBlock, direction, rotation, eyePosition, endOfRaytrace,
			hand, typeName, amountInHand, facing
		);
	}

	private BlockInteractEvent readBlockInteract() throws IOException {
		Position blockPosition = readBlockPosition(stream);
		Rotation rotation = readRotation(stream);
		boolean failedBlockPlacement = stream.readBoolean();
		Hand hand = readEnum("hand", Hand.values(), stream.readUnsignedByte());
		String blockType = stream.readUTF();
		String itemInHand = stream.readUTF();
		int amountInHand = stream.readInt();
		return new BlockInteractEvent(
			blockPosition, rotation, failedBlockPlacement,
			hand, blockType, itemInHand, amountInHand
		);
	}

	private WindowClickEvent readWindowClick() throws IOException {
		return new WindowClickEvent(
			stream.readInt(), stream.readInt(),
			stream.readInt(), stream.readInt(), stream.readInt(),
			stream.readUTF(), stream.readInt(), stream.readLong()
		);
	}

	private WindowItemsEvent readWindowItems() throws IOException {
		int windowId = stream.readInt();
		int count = readCollectionSize("window slots", stream.readInt());
		int size = readCollectionSize("window items", stream.readInt());
		Map<Integer, Item> items = new HashMap<>();
		for (int i = 0; i < size; i++) {
			items.put(stream.readInt(), readItem(stream));
		}
		return new WindowItemsEvent(windowId, count, items);
	}

	private WindowActionEvent readWindowAction() throws IOException {
		int windowId = stream.readInt();
		String actionName = stream.readUTF();
		WindowActionEvent.Action action;
		try {
			action = WindowActionEvent.Action.valueOf(actionName);
		} catch (IllegalArgumentException exception) {
			throw new IOException("Unknown window action: " + actionName, exception);
		}

		int length = stream.readInt();
		if (length != 4) {
			throw new IOException("Invalid armor contents length: " + length);
		}
		Item[] armorContents = new Item[length];
		for (int i = 0; i < length; i++) {
			if (stream.readBoolean()) {
				armorContents[i] = readItem(stream);
			}
		}
		return new WindowActionEvent(windowId, action, armorContents);
	}

	private Item readItem(DataInput in) throws IOException {
		String type = in.readUTF();
		int amount = in.readInt();
		int categoryId = in.readInt();
		ItemCategory category = null;
		for (ItemCategory candidate : ItemCategory.values()) {
			if (candidate.id() == categoryId) {
				category = candidate;
				break;
			}
		}
		if (category == null) {
			throw new IOException("Unknown item category: " + categoryId);
		}
		return new Item(
			type, amount, category, in.readBoolean(), in.readDouble(), in.readDouble()
		);
	}

	private void readEventSeparator() throws IOException {
		if (eventSeparator >= 0) {
			int actual = stream.readUnsignedByte();
			if (actual != eventSeparator) {
				throw new IOException(
					"Invalid event separator: expected " + eventSeparator + ", got " + actual
				);
			}
			return;
		}
		if (eventSeparator == NO_SEPARATOR) {
			return;
		}

		// Some 2024 production recordings append 0x0A without setting the
		// corresponding header flag. Probe the first event without consuming the
		// next duration byte when the recording has no separator.
		int candidate = input.read();
		if (candidate == '\n') {
			eventSeparator = '\n';
		} else {
			eventSeparator = NO_SEPARATOR;
			if (candidate >= 0) {
				input.unread(candidate);
			}
		}
	}

	private static int readCollectionSize(String name, int size) throws IOException {
		if (size < 0 || size > MAX_COLLECTION_SIZE) {
			throw new IOException("Invalid " + name + " count: " + size);
		}
		return size;
	}

	private static int readOrdinal(String name, int ordinal, int count) throws IOException {
		if (ordinal < 0 || ordinal >= count) {
			throw new IOException("Unknown " + name + ": " + ordinal);
		}
		return ordinal;
	}

	private static <T> T readEnum(String name, T[] values, int ordinal) throws IOException {
		return values[readOrdinal(name, ordinal, values.length)];
	}

	private static Direction readDirection(int ordinal) throws IOException {
		return DIRECTION_WIRE_ORDER[
			readOrdinal("direction", ordinal, DIRECTION_WIRE_ORDER.length)
		];
	}

	private static Position readPosition(DataInput in) throws IOException {
		return new Position(in.readDouble(), in.readDouble(), in.readDouble());
	}

	private static Position readBlockPosition(DataInput in) throws IOException {
		return new Position(in.readInt(), in.readInt(), in.readInt());
	}

	private static Rotation readRotation(DataInput in) throws IOException {
		return new Rotation(in.readFloat(), in.readFloat());
	}

	private static double conditionalReadDouble(
		DataInput in, int flags, int flag, double defaultValue
	) throws IOException {
		return (flags & flag) != 0 ? in.readDouble() : defaultValue;
	}

	private static float conditionalReadFloat(
		DataInput in, int flags, int flag, float defaultValue
	) throws IOException {
		return (flags & flag) != 0 ? in.readFloat() : defaultValue;
	}

	private static final class Flag {
		private static final int X = 1;
		private static final int Y = 1 << 1;
		private static final int Z = 1 << 2;
		private static final int YAW = 1 << 3;
		private static final int PITCH = 1 << 4;

		private Flag() {
		}
	}
}
