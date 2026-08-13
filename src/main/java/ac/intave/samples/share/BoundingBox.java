package ac.intave.samples.share;

public final class BoundingBox {
	private final double minX, minY, minZ;
	private final double maxX, maxY, maxZ;

	public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public boolean intersects(BoundingBox other) {
		return minX < other.maxX && maxX > other.minX &&
			   minY < other.maxY && maxY > other.minY &&
			   minZ < other.maxZ && maxZ > other.minZ;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + Double.hashCode(minX);
		result = 31 * result + Double.hashCode(minY);
		result = 31 * result + Double.hashCode(minZ);
		result = 31 * result + Double.hashCode(maxX);
		result = 31 * result + Double.hashCode(maxY);
		result = 31 * result + Double.hashCode(maxZ);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BoundingBox)) return false;
		BoundingBox other = (BoundingBox) obj;
		return Double.compare(minX, other.minX) == 0 &&
			   Double.compare(minY, other.minY) == 0 &&
			   Double.compare(minZ, other.minZ) == 0 &&
			   Double.compare(maxX, other.maxX) == 0 &&
			   Double.compare(maxY, other.maxY) == 0 &&
			   Double.compare(maxZ, other.maxZ) == 0;
	}

	@Override
	public String toString() {
		return "BoundingBox{" +
			   "minX=" + minX +
			   ", minY=" + minY +
			   ", minZ=" + minZ +
			   ", maxX=" + maxX +
			   ", maxY=" + maxY +
			   ", maxZ=" + maxZ +
			   '}';
	}
}
