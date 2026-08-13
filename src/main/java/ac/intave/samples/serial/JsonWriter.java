package ac.intave.samples.serial;

import ac.intave.samples.event.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class JsonWriter extends EventSink implements Flushable {
	public static final int MOVEMENT_EVENTS_PER_FLUSH = 1200;

	private final Gson gson = new GsonBuilder().disableJdkUnsafe().create();
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
		} catch (IOException | JsonIOException e) {
			throw new RuntimeException("Unable to write event " + eventType.name() + " to JSON writer", e);
		}
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
