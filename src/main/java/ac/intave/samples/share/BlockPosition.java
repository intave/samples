package ac.intave.samples.share;

public final class BlockPosition {
	private final int x;
	private final int y;
	private final int z;

	public BlockPosition(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int z() {
		return z;
	}

	@Override
	public int hashCode() {
		int result = Integer.hashCode(x);
		result = 31 * result + Integer.hashCode(y);
		result = 31 * result + Integer.hashCode(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BlockPosition)) return false;
		BlockPosition other = (BlockPosition) obj;
		return this.x == other.x && this.y == other.y && this.z == other.z;
	}

	@Override
	public String toString() {
		return "BlockPosition{" +
			"x=" + x +
			", y=" + y +
			", z=" + z +
			'}';
	}
}
