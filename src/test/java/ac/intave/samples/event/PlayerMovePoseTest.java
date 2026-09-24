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
import ac.intave.samples.share.Position;
import ac.intave.samples.share.Rotation;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

final class PlayerMovePoseTest {
  @Test
  void roundTripsKnownAndFuturePoseNames() throws Exception {
    for (String pose : Arrays.asList("STANDING", "CROUCHING", "SWIMMING", "FALL_FLYING", "FUTURE_POSE")) {
      PlayerMoveEvent event = movement(pose);
      event.withOffset(13);
      String output = write(event);
      JsonObject data = JsonParser.parseString(output).getAsJsonObject().getAsJsonObject("data");
      assertEquals(pose, data.get("pose").getAsString());
      try (JsonReader reader = new JsonReader(new StringReader(output))) {
        PlayerMoveEvent decoded = assertInstanceOf(PlayerMoveEvent.class, reader.nextEvent());
        assertEquals(pose, decoded.pose());
        assertEquals(13, decoded.offset());
        assertEquals(event, decoded);
        assertEquals(event.hashCode(), decoded.hashCode());
        assertTrue(decoded.toString().contains("pose=" + pose));
        assertNull(reader.nextEvent());
      }
      PlayerMoveEvent coordinates = PlayerMoveEvent.create(0, 1, 1, 2, 3, 90, -30,
        false, true, false, false, false, false, false, false, true, false, pose);
      assertEquals(event, coordinates);
    }
  }

  @Test
  void olderSamplesAndConstructorsLeavePoseUnknown() throws Exception {
    String legacy = "{\"type\":\"player.move\",\"data\":{\"sneaking\":true,\"inWater\":true}}\n";
    try (JsonReader reader = new JsonReader(new StringReader(legacy))) {
      PlayerMoveEvent decoded = assertInstanceOf(PlayerMoveEvent.class, reader.nextEvent());
      assertNull(decoded.pose());
      assertTrue(decoded.sneaking());
      assertTrue(decoded.inWater());
    }
    PlayerMoveEvent previous = PlayerMoveEvent.create(0, 1, new Position(1, 2, 3), new Rotation(90, -30),
      false, true, false, false, false, false, false, false, true, false);
    assertNull(previous.pose());
    assertNull(new PlayerMoveEvent().pose());
    assertEquals(movement(null), previous);
    JsonObject data = JsonParser.parseString(write(previous)).getAsJsonObject().getAsJsonObject("data");
    assertFalse(data.has("pose"));
  }

  @Test
  void poseParticipatesInMovementEquality() {
    PlayerMoveEvent standing = movement("STANDING");
    PlayerMoveEvent swimming = movement("SWIMMING");
    PlayerMoveEvent unknown = movement(null);
    assertNotEquals(standing, swimming);
    assertNotEquals(standing, unknown);
    assertNotEquals(swimming, unknown);
    assertEquals(3, new HashSet<>(Arrays.asList(standing, swimming, unknown)).size());
  }

  private PlayerMoveEvent movement(String pose) {
    return PlayerMoveEvent.create(0, 1, new Position(1, 2, 3), new Rotation(90, -30),
      false, true, false, false, false, false, false, false, true, false, pose);
  }

  private String write(Event event) {
    StringWriter output = new StringWriter();
    try (JsonWriter writer = new JsonWriter(output)) {
      event.accept(writer);
    }
    return output.toString();
  }
}
