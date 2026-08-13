package ac.intave.samples.share;

public final class Position {
	public final static Position ZERO = new Position(0, 0, 0);

	private final double x;
	private final double y;
	private final double z;

	public Position(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public double z() {
		return z;
	}

	@Override
	public String toString() {
		return "Position{" +
			"x=" + x +
			", y=" + y +
			", z=" + z +
			'}';
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + Double.hashCode(x);
		result = 31 * result + Double.hashCode(y);
		result = 31 * result + Double.hashCode(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Position other = (Position) obj;
		return Double.compare(x, other.x) == 0 &&
				Double.compare(y, other.y) == 0 &&
				Double.compare(z, other.z) == 0;
	}

	public static Position of(double x, double y, double z) {
		return new Position(x, y, z);
	}
}
