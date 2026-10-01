package ac.intave.samples.event;

import ac.intave.samples.share.Block;
import ac.intave.samples.share.BlockUpdate;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class BlockUpdatesEvent extends Event implements ChunkableEvent<BlockUpdate> {
	private static final int EVENT_ENVELOPE_MAX_CHARACTERS = 256;
	private static final int BLOCK_UPDATE_MAX_CHARACTERS = 256;
	private static final int BOUNDING_BOX_MAX_CHARACTERS = 256;

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

	@Override
	public String chunkFieldName() {
		return "updates";
	}

	@Override
	public List<BlockUpdate> chunkValues() {
		return updates;
	}

	@Override
	public Event chunkWith(Collection<BlockUpdate> values) {
		return new BlockUpdatesEvent(values);
	}

	@Override
	public boolean couldExceedCharacterLimit(int characterLimit) {
		long characters = EVENT_ENVELOPE_MAX_CHARACTERS;
		for (BlockUpdate update : updates) {
			characters += BLOCK_UPDATE_MAX_CHARACTERS;
			if (update != null) {
				Block block = update.block();
				if (block != null) {
					characters += jsonStringMaxCharacters(block.name());
					Map<String, String> properties = block.properties();
					if (properties != null) {
						for (Map.Entry<String, String> property : properties.entrySet()) {
							characters += 16L +
								jsonStringMaxCharacters(property.getKey()) +
								jsonStringMaxCharacters(property.getValue());
						}
					}
					if (block.boundingBoxes() != null) {
						characters +=
							(long) block.boundingBoxes().size() * BOUNDING_BOX_MAX_CHARACTERS;
					}
				}
			}
			if (characters > characterLimit) {
				return true;
			}
		}
		return false;
	}

	private static long jsonStringMaxCharacters(String value) {
		return value == null ? 4L : 2L + 6L * value.length();
	}
}
