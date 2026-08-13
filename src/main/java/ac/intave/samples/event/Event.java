package ac.intave.samples.event;

import com.google.gson.annotations.SerializedName;

public abstract class Event {
	@SerializedName("offset")
	private long offset;

	public void accept(EventSink sink) {
		sink.visitSelect(this);
	}

	public long offset() {
		return offset;
	}

	public void withOffset(long offset) {
		this.offset = offset;
	}
}
