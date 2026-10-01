package ac.intave.samples.serial;

import ac.intave.samples.event.*;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JsonWriter extends EventSink implements Flushable {
	public static final int MOVEMENT_EVENTS_PER_FLUSH = 1200;
	private static final String CHUNK_SUFFIX = "]}}";

	private final Gson gson = new Gson();
	private final Writer writer;
	private int movementEventsSinceFlush;
	private boolean closed;

	public JsonWriter(OutputStream stream) throws IOException {
		this(new BufferedWriter(new OutputStreamWriter(ZstdStreams.compressionStream(stream), StandardCharsets.UTF_8)));
	}

	public JsonWriter(Writer writer) {
		this.writer = writer;
	}

	@Override
	public void visitAny(Event event) {
		EventType<? extends Event> eventType = EventRegistry.typeOf(event);
		try {
			if (event instanceof ChunkableEvent &&
				((ChunkableEvent<?>) event).couldExceedCharacterLimit(
					JsonReader.MAX_EVENT_CHARACTERS)) {
				writeChunks(event, (ChunkableEvent<?>) event, eventType);
			} else {
				writeEvent(event, eventType);
			}
		} catch (IOException | JsonIOException e) {
			throw new RuntimeException("Unable to write event " + eventType.name() + " to JSON writer", e);
		}
	}

	private void writeEvent(Event event, EventType<? extends Event> eventType) throws IOException {
		JsonObject record = new JsonObject();
		record.addProperty("type", eventType.name());
		record.add("data", gson.toJsonTree(event));
		gson.toJson(record, writer);
		writer.write('\n');
		if (event instanceof PlayerMoveEvent &&
			++movementEventsSinceFlush >= MOVEMENT_EVENTS_PER_FLUSH) {
			writer.flush();
			movementEventsSinceFlush = 0;
		}
	}

	private <T> void writeChunks(
		Event event,
		ChunkableEvent<T> chunkable,
		EventType<? extends Event> eventType
	) throws IOException {
		String firstPrefix = chunkPrefix(chunkable, eventType, event.offset());
		String continuationPrefix = event.offset() == 0L ?
			firstPrefix : chunkPrefix(chunkable, eventType, 0L);
		int maximumPrefixCharacters = Math.max(
			firstPrefix.length(), continuationPrefix.length()
		);
		if (maximumPrefixCharacters + CHUNK_SUFFIX.length() >
			JsonReader.MAX_EVENT_CHARACTERS) {
			throw new IllegalArgumentException(
				"Chunk metadata for " + eventType.name() +
					" exceeds the JSON event character limit"
			);
		}

		List<T> values = chunkable.chunkValues();
		List<String> serializedValues = new ArrayList<>(values.size());
		for (T value : values) {
			String serializedValue = gson.toJson(value);
			if (maximumPrefixCharacters + serializedValue.length() + CHUNK_SUFFIX.length() >
				JsonReader.MAX_EVENT_CHARACTERS) {
				throw new IllegalArgumentException(
					"A single value in " + chunkable.chunkFieldName() +
						" exceeds the JSON event character limit"
				);
			}
			serializedValues.add(serializedValue);
		}

		String prefix = firstPrefix;
		int lineCharacters = prefix.length() + CHUNK_SUFFIX.length();
		boolean firstValue = true;
		writer.write(prefix);
		for (String serializedValue : serializedValues) {
			int requiredCharacters = serializedValue.length() + (firstValue ? 0 : 1);
			if (!firstValue && lineCharacters + requiredCharacters >
				JsonReader.MAX_EVENT_CHARACTERS) {
				writer.write(CHUNK_SUFFIX);
				writer.write('\n');
				prefix = continuationPrefix;
				lineCharacters = prefix.length() + CHUNK_SUFFIX.length();
				firstValue = true;
				writer.write(prefix);
			}
			if (!firstValue) {
				writer.write(',');
				lineCharacters++;
			}
			writer.write(serializedValue);
			lineCharacters += serializedValue.length();
			firstValue = false;
		}
		writer.write(CHUNK_SUFFIX);
		writer.write('\n');
	}

	private String chunkPrefix(
		ChunkableEvent<?> chunkable,
		EventType<? extends Event> eventType,
		long offset
	) {
		Event template = chunkable.chunkWith(Collections.emptyList());
		template.withOffset(offset);
		JsonObject data = gson.toJsonTree(template).getAsJsonObject();
		if (data.remove(chunkable.chunkFieldName()) == null) {
			throw new IllegalArgumentException(
				"Missing chunk field " + chunkable.chunkFieldName() +
					" in event " + eventType.name()
			);
		}
		String serializedData = gson.toJson(data);
		String separator = data.entrySet().isEmpty() ? "" : ",";
		return "{\"type\":" + gson.toJson(eventType.name()) + ",\"data\":" +
			serializedData.substring(0, serializedData.length() - 1) + separator +
			gson.toJson(chunkable.chunkFieldName()) + ":[";
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
		movementEventsSinceFlush = 0;
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Unable to close JSON writer", e);
		}
	}

	@Override
	public String name() {
		return "json";
	}
}
