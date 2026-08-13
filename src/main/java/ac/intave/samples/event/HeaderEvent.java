package ac.intave.samples.event;

import ac.intave.samples.share.Classifier;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public final class HeaderEvent extends Event {
	@SerializedName("id")
	private UUID id;
	@SerializedName("licenseName")
	private String licenseName;
	@SerializedName("classifier")
	private Classifier classifier;
	@SerializedName("createdAt")
	private long createdAt;

	public HeaderEvent() {
	}

	public HeaderEvent(UUID id, String licenseName, Classifier classifier, long createdAt) {
		this.id = id;
		this.licenseName = licenseName;
		this.classifier = classifier;
		this.createdAt = createdAt;
	}

	public UUID id() {
		return id;
	}

	public String licenseName() {
		return licenseName;
	}

	public Classifier classifier() {
		return classifier;
	}

	public long createdAt() {
		return createdAt;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + licenseName.hashCode();
		result = 31 * result + classifier.hashCode();
		result = 31 * result + Long.hashCode(createdAt);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof HeaderEvent)) return false;
		HeaderEvent other = (HeaderEvent) obj;
		return id.equals(other.id) && licenseName.equals(other.licenseName) && classifier == other.classifier && createdAt == other.createdAt;
	}

	@Override
	public String toString() {
		return "HeaderEvent{" +
			"id=" + id +
			", licenseName='" + licenseName + '\'' +
			", classifier=" + classifier +
			", createdAt=" + createdAt +
			'}';
	}
}
