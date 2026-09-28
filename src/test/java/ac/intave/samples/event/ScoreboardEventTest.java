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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class ScoreboardEventTest {
  @Test
  void roundTripsCombinedUpdateThroughRegisteredWireFormat() throws Exception {
    ScoreboardEvent event = new ScoreboardEvent(0, "\u00a7aKills: \"4\" \\ \u2605", "\u00a76Match\nStats");
    event.withOffset(42);
    StringWriter output = new StringWriter();
    try (JsonWriter writer = new JsonWriter(output)) {
      event.accept(writer);
    }

    JsonObject record = JsonParser.parseString(output.toString()).getAsJsonObject();
    assertEquals("scoreboard", record.get("type").getAsString());
    assertEquals(ScoreboardEvent.class, EventRegistry.typeNamed("scoreboard").eventClass());
    JsonObject data = record.getAsJsonObject("data");
    assertEquals(0, data.get("line").getAsInt());
    assertEquals(event.text(), data.get("text").getAsString());
    assertEquals(event.title(), data.get("title").getAsString());
    try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
      ScoreboardEvent decoded = assertInstanceOf(ScoreboardEvent.class, reader.nextEvent());
      assertEquals(event.line(), decoded.line());
      assertEquals(event.text(), decoded.text());
      assertEquals(event.title(), decoded.title());
      assertEquals(42, decoded.offset());
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void preservesPartialAndBlankUpdatesThroughCompressedRecording() throws Exception {
    List<ScoreboardEvent> events = Arrays.asList(
      new ScoreboardEvent("Match stats"),
      new ScoreboardEvent(0, "Kills: 3"),
      new ScoreboardEvent(1, "Deaths: 1"),
      new ScoreboardEvent(0, "Kills: 4", "Match over"),
      new ScoreboardEvent(1, ""),
      new ScoreboardEvent("")
    );
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (JsonWriter writer = new JsonWriter(output)) {
      for (int i = 0; i < events.size(); i++) {
        events.get(i).withOffset(i * 10L);
        events.get(i).accept(writer);
      }
    }
    try (JsonReader reader = new JsonReader(new ByteArrayInputStream(output.toByteArray()))) {
      for (ScoreboardEvent expected : events) {
        ScoreboardEvent decoded = assertInstanceOf(ScoreboardEvent.class, reader.nextEvent());
        assertEquals(expected.title(), decoded.title());
        assertEquals(expected.line(), decoded.line());
        assertEquals(expected.text(), decoded.text());
        assertEquals(expected.offset(), decoded.offset());
      }
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void omittedFieldsLeaveTitleOrLinesUnchanged() throws Exception {
    String input = "{\"type\":\"scoreboard\",\"data\":{\"title\":\"\"}}\n"
      + "{\"type\":\"scoreboard\",\"data\":{\"line\":0,\"text\":\"\"}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(input))) {
      ScoreboardEvent titleOnly = (ScoreboardEvent) reader.nextEvent();
      assertEquals("", titleOnly.title());
      assertNull(titleOnly.line());
      assertNull(titleOnly.text());
      ScoreboardEvent lineOnly = (ScoreboardEvent) reader.nextEvent();
      assertNull(lineOnly.title());
      assertEquals(Integer.valueOf(0), lineOnly.line());
      assertEquals("", lineOnly.text());
    }
  }

  @Test
  void dispatchesThroughTypedVisitorsAndGenericFallback() {
    ScoreboardEvent event = new ScoreboardEvent(0, "Kills: 4", "Match stats");
    List<Event> typed = new ArrayList<>();
    EventSink sink = new EventSink() {
      @Override public void visit(ScoreboardEvent received) { typed.add(received); }
      @Override public void visitAny(Event received) { fail("Typed visitor bypassed"); }
      @Override public String name() { return "typed"; }
    };
    List<Event> fallback = new ArrayList<>();
    EventSink generic = new EventSink() {
      @Override public void visitAny(Event received) { fallback.add(received); }
      @Override public String name() { return "generic"; }
    };

    event.accept(sink);
    sink.visitSelect(event);
    event.accept(generic);
    generic.visitSelect(event);

    assertEquals(Arrays.asList(event, event), typed);
    assertEquals(typed, fallback);
  }

  @Test
  void rejectsInvalidConstructorArguments() {
    assertThrows(IllegalArgumentException.class, () -> new ScoreboardEvent(-1, "text"));
    assertThrows(NullPointerException.class, () -> new ScoreboardEvent(0, null));
    assertThrows(NullPointerException.class, () -> new ScoreboardEvent((String) null));
    assertThrows(NullPointerException.class, () -> new ScoreboardEvent(0, "text", null));
  }
}
