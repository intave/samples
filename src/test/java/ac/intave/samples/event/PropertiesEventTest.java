package ac.intave.samples.event;

import ac.intave.samples.serial.JsonReader;
import ac.intave.samples.serial.JsonWriter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesEventTest {
  @Test
  void roundTripsStringProperties() throws Exception {
    for (String version : new String[] {"LEGACY", "MODERN", "UNKNOWN"}) {
      Map<String, String> properties = new HashMap<>();
      properties.put("combatVersion", version);
      properties.put("reducingDisabled", "false");
      properties.put("threshold", "0.75");
      properties.put("empty", "");
      properties.put("description", "quoted \"value\"\nnext line\t\u2603");
      PropertiesEvent event = new PropertiesEvent(properties);
      event.withOffset(17);
      StringWriter output = new StringWriter();
      try (JsonWriter writer = new JsonWriter(output)) {
        writer.visitAny(event);
      }
      assertTrue(output.toString().contains("\"combatVersion\":\"" + version + "\""));
      JsonObject encoded = JsonParser.parseString(output.toString()).getAsJsonObject()
        .getAsJsonObject("data").getAsJsonObject("properties");
      properties.forEach((name, value) -> {
        assertTrue(encoded.getAsJsonPrimitive(name).isString());
        assertEquals(value, encoded.get(name).getAsString());
      });
      try (JsonReader reader = new JsonReader(new StringReader(output.toString()))) {
        PropertiesEvent decoded = assertInstanceOf(PropertiesEvent.class, reader.nextEvent());
        assertEquals(properties, decoded.properties());
        assertEquals(17, decoded.offset());
        assertNull(reader.nextEvent());
      }
    }
  }

  @Test
  void readsExistingBooleanOnlyRecordings() throws Exception {
    String json = "{\"type\":\"environment.properties\",\"data\":{\"properties\":{"
      + "\"reducingDisabled\":true,\"otherRule\":false}}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(json))) {
      PropertiesEvent decoded = assertInstanceOf(PropertiesEvent.class, reader.nextEvent());
      assertEquals("true", decoded.properties().get("reducingDisabled"));
      assertEquals("false", decoded.properties().get("otherRule"));
      assertFalse(decoded.properties().containsKey("combatVersion"));
    }
  }

  @Test
  void snapshotsTheInputMap() {
    Map<String, String> properties = new HashMap<>();
    properties.put("reducingDisabled", "true");
    PropertiesEvent event = new PropertiesEvent(properties);
    properties.put("reducingDisabled", "false");
    assertEquals("true", event.properties().get("reducingDisabled"));
  }
}
