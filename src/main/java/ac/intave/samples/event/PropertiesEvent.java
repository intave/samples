package ac.intave.samples.event;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * String-valued environment metadata. Legacy JSON boolean values are read as
 * {@code "true"} or {@code "false"} by Gson's string adapter.
 * Consumers of the former boolean-valued API must be updated and recompiled;
 * boolean-only readers cannot preserve arbitrary string values.
 */
public final class PropertiesEvent extends Event {
  @SerializedName("properties")
  private final Map<String, String> properties = new HashMap<>();

  public PropertiesEvent() {
  }

  public PropertiesEvent(Map<String, String> properties) {
    this.properties.putAll(properties);
  }

  public Map<String, String> properties() {
    return properties;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
