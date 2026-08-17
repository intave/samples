package ac.intave.samples.event;

import ac.intave.samples.share.BlockUpdate;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BlockUpdatesEvent extends Event {
	@SerializedName("updates")
	private final List<BlockUpdate> updates = new ArrayList<>();

	public BlockUpdatesEvent() {
	}

	public BlockUpdatesEvent(Collection<BlockUpdate> updates) {
		this.updates.addAll(updates);
	}

	public List<BlockUpdate> updates() {
		return updates;
	}

	@Override
	public void accept(EventSink sink) {
		sink.visit(this);
	}
}
