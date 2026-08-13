package ac.intave.samples.share;

public enum Classifier {
	CHEAT,
	LEGIT,
	UNKNOWN;

	public static Classifier fromFlags(int flags) {
		return (flags & 2) != 0 ? CHEAT : (flags & 1) != 0 ? LEGIT : UNKNOWN;
	}
}
