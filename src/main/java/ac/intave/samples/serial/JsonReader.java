package ac.intave.samples.serial;

import ac.intave.samples.event.Event;
import ac.intave.samples.event.EventRegistry;
import ac.intave.samples.event.EventType;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Streams events from JSON Lines records containing {@code type} and
 * {@code data} fields.
 */
public final class JsonReader implements Closeable {
	public static final int MAX_EVENT_CHARACTERS = 25_000;

	private final BufferedReader reader;
	private final Gson GSON = new Gson();
	private int lineNumber;
	private boolean finished;

	public JsonReader(InputStream stream) throws IOException {
		this(new InputStreamReader(ZstdStreams.decompressionStream(stream), StandardCharsets.UTF_8));
	}

	public JsonReader(Reader reader) {
		this.reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}

	public Event nextEvent() throws IOException {
		if (finished) {
			return null;
		}
		String line = readEventLine();
		if (line == null) {
			finished = true;
			return null;
		}
		lineNumber++;
		if (line.trim().isEmpty()) {
			throw new IOException("Invalid JSONL event at line " + lineNumber + ": empty line");
		}
		JsonObject record;
		try {
			JsonElement parsed = JsonParser.parseString(line);
			if (!parsed.isJsonObject()) {
				throw new IOException("Invalid JSONL event at line " + lineNumber + ": record is not a JSON object");
			}
			record = parsed.getAsJsonObject();
		} catch (JsonParseException | IllegalStateException exception) {
			throw new IOException("Invalid JSONL event at line " + lineNumber + ": malformed JSON", exception);
		}

		String typeName = readType(record);
		EventType<? extends Event> eventType = EventRegistry.typeNamed(typeName);
		if (eventType == null) {
			throw new IOException("Invalid JSONL event at line " + lineNumber + ": unknown event type: " + typeName);
		}

		JsonElement data = record.get("data");
		if (data == null || !data.isJsonObject()) {
			throw new IOException("Invalid JSONL event at line " + lineNumber + ": field 'data' must be a JSON object");
		}
		try {
			Event event = GSON.fromJson(data, eventType.eventClass());
			if (event == null) {
				throw new IOException("Invalid JSONL event at line " + lineNumber + ": field 'data' contains null");
			}
			return event;
		} catch (JsonParseException | IllegalStateException exception) {
			throw new IOException("Invalid JSONL event at line " + lineNumber + ": invalid data for event type " + typeName, exception);
		}
	}

	private String readType(JsonObject record) throws IOException {
		JsonElement element = record.get("type");
		if (element == null || !element.isJsonPrimitive()) {
			throw new IOException("Invalid JSONL event at line " + lineNumber + ": field 'type' must be a string");
		}
		JsonPrimitive primitive = element.getAsJsonPrimitive();
		if (!primitive.isString()) {
			throw new IOException("Invalid JSONL event at line " + lineNumber + ": field 'type' must be a string");
		}
		return primitive.getAsString();
	}

	private String readEventLine() throws IOException {
		char[] characters = new char[MAX_EVENT_CHARACTERS];
		int length = 0;
		boolean pendingCarriageReturn = false;
		while (true) {
			int value = reader.read();
			if (value == -1) {
				if (pendingCarriageReturn) {
					if (length == characters.length) {
						finished = true;
						throw new IOException("Invalid JSONL event at line " + (lineNumber + 1) + ": event exceeds " + MAX_EVENT_CHARACTERS + " characters");
					}
					characters[length++] = '\r';
				}
				return length == 0 ? null : new String(characters, 0, length);
			}
			if (value == '\n') {
				return new String(characters, 0, length);
			}
			if (pendingCarriageReturn) {
				if (length == characters.length) {
					finished = true;
					throw new IOException("Invalid JSONL event at line " + (lineNumber + 1) + ": event exceeds " + MAX_EVENT_CHARACTERS + " characters");
				}
				characters[length++] = '\r';
				pendingCarriageReturn = false;
			}
			if (value == '\r') {
				pendingCarriageReturn = true;
				continue;
			}
			if (length == characters.length) {
				finished = true;
				throw new IOException("Invalid JSONL event at line " + (lineNumber + 1) + ": event exceeds " + MAX_EVENT_CHARACTERS + " characters");
			}
			characters[length++] = (char) value;
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
