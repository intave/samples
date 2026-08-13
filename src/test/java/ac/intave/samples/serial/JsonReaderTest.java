package ac.intave.samples.serial;

import ac.intave.samples.event.ClickEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class JsonReaderTest {
	@Test
	void readsEventAndReturnsNullAtEndOfStream() throws IOException {
		String jsonl = "{\"type\":\"input.click\",\"data\":{\"offset\":7}}\n";
		try (JsonReader reader = new JsonReader(new StringReader(jsonl))) {
			ClickEvent event = assertInstanceOf(ClickEvent.class, reader.nextEvent());
			assertNotNull(event);
			assertEquals(7, event.offset());
			assertNull(reader.nextEvent());
			assertNull(reader.nextEvent());
		}
	}

	@Test
	void rejectsUnknownTypeWithLineNumber() throws IOException {
		String jsonl = "{\"type\":\"missing.event\",\"data\":{}}\n";
		try (JsonReader reader = new JsonReader(new StringReader(jsonl))) {
			IOException exception = assertThrows(IOException.class, reader::nextEvent);
			assertTrue(exception.getMessage().contains("line 1"));
			assertTrue(exception.getMessage().contains("unknown event type"));
		}
	}

	@Test
	void rejectsEmptyLines() throws IOException {
		try (JsonReader reader = new JsonReader(new StringReader("\n"))) {
			IOException exception = assertThrows(IOException.class, reader::nextEvent);
			assertTrue(exception.getMessage().contains("empty line"));
		}
	}

	@Test
	void acceptsAnEventAtTheCharacterLimit() throws IOException {
		String event = "{\"type\":\"input.click\",\"data\":{}}";
		String jsonl = event
			+ repeat(' ', JsonReader.MAX_EVENT_CHARACTERS - event.length())
			+ "\r\n";
		try (JsonReader reader = new JsonReader(new StringReader(jsonl))) {
			assertInstanceOf(ClickEvent.class, reader.nextEvent());
			assertNull(reader.nextEvent());
		}
	}

	@Test
	void rejectsAnOversizedEventBeforeWaitingForANewline() throws IOException {
		String firstEvent = "{\"type\":\"input.click\",\"data\":{}}\n";
		String oversizedEvent = repeat('x', JsonReader.MAX_EVENT_CHARACTERS + 1);
		try (JsonReader reader = new JsonReader(new StringReader(firstEvent + oversizedEvent))) {
			assertInstanceOf(ClickEvent.class, reader.nextEvent());
			IOException exception = assertThrows(IOException.class, reader::nextEvent);
			assertTrue(exception.getMessage().contains("line 2"));
			assertTrue(exception.getMessage().contains("exceeds 25000 characters"));
			assertNull(reader.nextEvent(), "reader should stop after an oversized event");
		}
	}

	private static String repeat(char character, int count) {
		char[] characters = new char[count];
		java.util.Arrays.fill(characters, character);
		return new String(characters);
	}
}
