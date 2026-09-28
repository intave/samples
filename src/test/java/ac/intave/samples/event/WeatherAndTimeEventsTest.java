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
import ac.intave.samples.share.ClockState;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class WeatherAndTimeEventsTest {
  @Test
  void roundTripsWeatherAndPlayerTimeThroughCompressedRecording() throws Exception {
    WeatherEvent weather = new WeatherEvent(true, null, 0.625F, 0.375F);
    weather.withOffset(50);
    TimeEvent time = new TimeEvent(42L * 24_000 + 18_000, false);
    time.withOffset(20);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (JsonWriter writer = new JsonWriter(output)) {
      weather.accept(writer);
      time.accept(writer);
    }
    try (JsonReader reader = new JsonReader(new ByteArrayInputStream(output.toByteArray()))) {
      WeatherEvent decodedWeather = assertInstanceOf(WeatherEvent.class, reader.nextEvent());
      assertTrue(decodedWeather.raining());
      assertNull(decodedWeather.thundering());
      assertEquals(0.625F, decodedWeather.rainLevel());
      assertEquals(0.375F, decodedWeather.thunderLevel());
      assertEquals(50, decodedWeather.offset());
      TimeEvent decodedTime = assertInstanceOf(TimeEvent.class, reader.nextEvent());
      assertEquals(time.time(), decodedTime.time());
      assertFalse(decodedTime.ticking());
      assertNull(decodedTime.clocks());
      assertEquals(20, decodedTime.offset());
      assertNull(reader.nextEvent());
    }
  }

  @Test
  void retainsPlayerSpecificNamedClockStates() throws Exception {
    Map<String, ClockState> clocks = new LinkedHashMap<>();
    clocks.put("minecraft:overworld", new ClockState(Long.MAX_VALUE, 0.25F, 0));
    clocks.put("custom:moon", new ClockState(-24000, 0.75F, 2.5F));
    TimeEvent time = new TimeEvent(null, null, clocks);
    clocks.clear();
    StringWriter output = new StringWriter();
    try (JsonWriter writer = new JsonWriter(output)) {
      time.accept(writer);
    }
    assertTrue(output.toString().contains("\"type\":\"environment.time\""));
    assertFalse(output.toString().contains("gameTime"));
    try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
      TimeEvent decoded = assertInstanceOf(TimeEvent.class, reader.nextEvent());
      assertNull(decoded.time());
      assertNull(decoded.ticking());
      assertEquals(2, decoded.clocks().size());
      ClockState overworld = decoded.clocks().get("minecraft:overworld");
      assertEquals(Long.MAX_VALUE, overworld.time());
      assertEquals(0.25F, overworld.partialTick());
      assertEquals(0, overworld.rate());
      ClockState moon = decoded.clocks().get("custom:moon");
      assertEquals(-24000, moon.time());
      assertEquals(0.75F, moon.partialTick());
      assertEquals(2.5F, moon.rate());
      assertThrows(UnsupportedOperationException.class, () -> decoded.clocks().clear());
    }
  }

  @Test
  void distinguishesUnknownFromClearWeatherAndFrozenZeroTime() throws Exception {
    String input = "{\"type\":\"environment.weather\",\"data\":{}}\n"
      + "{\"type\":\"environment.time\",\"data\":{}}\n"
      + "{\"type\":\"environment.weather\",\"data\":{\"raining\":false,\"thundering\":false,\"rainLevel\":0,\"thunderLevel\":0}}\n"
      + "{\"type\":\"environment.time\",\"data\":{\"time\":0,\"ticking\":false}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(input))) {
      WeatherEvent unknownWeather = (WeatherEvent) reader.nextEvent();
      assertNull(unknownWeather.raining());
      assertNull(unknownWeather.thundering());
      assertNull(unknownWeather.rainLevel());
      assertNull(unknownWeather.thunderLevel());
      TimeEvent unknownTime = (TimeEvent) reader.nextEvent();
      assertNull(unknownTime.time());
      assertNull(unknownTime.ticking());
      assertNull(unknownTime.clocks());
      WeatherEvent clear = (WeatherEvent) reader.nextEvent();
      assertFalse(clear.raining());
      assertFalse(clear.thundering());
      assertEquals(0.0F, clear.rainLevel());
      assertEquals(0.0F, clear.thunderLevel());
      TimeEvent frozen = (TimeEvent) reader.nextEvent();
      assertEquals(0L, frozen.time());
      assertFalse(frozen.ticking());
    }
  }

  @Test
  void decodesLegacyFrozenPlayerTime() {
    assertLegacy(0, 0, true);
    assertLegacy(18000, 18000, true);
    assertLegacy(-18000, 18000, false);
    assertLegacy(-1, 1, false);
    assertLegacy(-Long.MAX_VALUE, Long.MAX_VALUE, false);
    assertThrows(IllegalArgumentException.class, () -> TimeEvent.fromLegacyPacket(Long.MIN_VALUE));
  }

  @Test
  void readsInterimFieldNamesWithoutRestoringWorldAge() throws Exception {
    String input = "{\"type\":\"environment.time\",\"data\":{"
      + "\"gameTime\":999,\"dayTime\":18000,\"tickDayTime\":false}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(input))) {
      TimeEvent decoded = (TimeEvent) reader.nextEvent();
      assertEquals(18000L, decoded.time());
      assertFalse(decoded.ticking());
    }
  }

  @Test
  void dispatchesThroughTypedVisitorsAndGenericFallback() {
    List<Event> typed = new ArrayList<>();
    EventSink sink = new EventSink() {
      @Override public void visit(WeatherEvent event) { typed.add(event); }
      @Override public void visit(TimeEvent event) { typed.add(event); }
      @Override public void visitAny(Event event) { fail("Typed visitor bypassed"); }
      @Override public String name() { return "typed"; }
    };
    List<Event> fallback = new ArrayList<>();
    EventSink generic = new EventSink() {
      @Override public void visitAny(Event event) { fallback.add(event); }
      @Override public String name() { return "generic"; }
    };
    for (Event event : Arrays.asList(new WeatherEvent(), new TimeEvent())) {
      event.accept(sink);
      sink.visitSelect(event);
      event.accept(generic);
      generic.visitSelect(event);
    }
    assertEquals(4, typed.size());
    assertEquals(typed, fallback);
    assertEquals("environment.weather", EventRegistry.typeOf(new WeatherEvent()).name());
    assertEquals(TimeEvent.class, EventRegistry.typeNamed("environment.time").eventClass());
  }

  private static void assertLegacy(long raw, long expectedTime, boolean ticking) {
    TimeEvent event = TimeEvent.fromLegacyPacket(raw);
    assertEquals(expectedTime, event.time());
    assertEquals(ticking, event.ticking());
  }
}
