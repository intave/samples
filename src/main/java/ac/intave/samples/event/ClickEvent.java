package ac.intave.samples.event;

public final class ClickEvent extends Event {
  private static final ClickEvent SINGLETON = new ClickEvent();

  public ClickEvent() {
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public static ClickEvent create() {
    return SINGLETON;
  }
}
