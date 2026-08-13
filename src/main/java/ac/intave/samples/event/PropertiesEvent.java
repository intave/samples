package ac.intave.samples.event;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public final class PropertiesEvent extends Event {
  @SerializedName("properties")
  private final Map<String, Boolean> properties = new HashMap<>();

  public PropertiesEvent() {
  }

  public PropertiesEvent(Map<String, Boolean> properties) {
    this.properties.putAll(properties);
  }

  public Map<String, Boolean> properties() {
    return properties;
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }
}
