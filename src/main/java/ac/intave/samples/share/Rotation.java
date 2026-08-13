package ac.intave.samples.share;

public final class Rotation {
	public static final Rotation ZERO = new Rotation(0, 0);

	private final float yaw;
	private final float pitch;

	public Rotation(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public float yaw() {
		return yaw;
	}

	public float pitch() {
		return pitch;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + Float.hashCode(yaw);
		result = 31 * result + Float.hashCode(pitch);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Rotation)) return false;
		Rotation other = (Rotation) obj;
		return Float.compare(yaw, other.yaw) == 0 && Float.compare(pitch, other.pitch) == 0;
	}

	@Override
	public String toString() {
		return "Rotation{" +
			"yaw=" + yaw +
			", pitch=" + pitch +
			'}';
	}

	public static Rotation of(float yaw, float pitch) {
		return new Rotation(yaw, pitch);
	}
}
