package ac.intave.samples.share;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public final class BlockUpdate {
	@SerializedName("position")
	private BlockPosition position = new BlockPosition(0, 0, 0);
	@SerializedName("block")
	private Block block = Block.AIR;

	public BlockUpdate() {
	}

	public BlockUpdate(BlockPosition position, Block block) {
		this.position = Objects.requireNonNull(position, "position");
		this.block = Objects.requireNonNull(block, "block");
	}

	public BlockPosition position() {
		return position;
	}

	public Block block() {
		return block;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof BlockUpdate)) {
			return false;
		}
		BlockUpdate other = (BlockUpdate) object;
		return position.equals(other.position) && block.equals(other.block);
	}

	@Override
	public int hashCode() {
		int result = position.hashCode();
		result = 31 * result + block.hashCode();
		return result;
	}
}
