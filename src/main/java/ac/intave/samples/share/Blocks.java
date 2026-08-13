package ac.intave.samples.share;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Blocks {
	private final Map<BlockPosition, Block> contents = new HashMap<>();

	public void fill(
		Position playerPosition, int radius,
		Function<BlockPosition, Block> blockProvider
	) {
		int minX = (int) (playerPosition.x() - radius);
		int maxX = (int) (playerPosition.x() + radius);
		int minY = (int) (playerPosition.y() - radius);
		int maxY = (int) (playerPosition.y() + radius);
		int minZ = (int) (playerPosition.z() - radius);
		int maxZ = (int) (playerPosition.z() + radius);
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					BlockPosition pos = new BlockPosition(x, y, z);
					Block block = blockProvider.apply(pos);
					setBlock(x, y, z, block);
				}
			}
		}
	}

	public void setBlock(int x, int y, int z, Block block) {
		contents.put(new BlockPosition(x, y, z), block);
	}

	public Block getBlock(int x, int y, int z) {
		return contents.getOrDefault(new BlockPosition(x, y, z), Block.AIR);
	}

	public static Blocks empty() {
		return new Blocks();
	}
}

