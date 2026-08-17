package ac.intave.samples.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Block {
	public static final Block AIR = new Block("AIR", new HashMap<>(), new ArrayList<>());

	private final String name;
	private final Map<String, String> properties;
 	private final List<BoundingBox> boundingBoxes;

	public Block(
		String name, Map<String, String> properties, List<BoundingBox> boundingBoxes
	) {
		this.name = name;
		this.properties = properties;
		this.boundingBoxes = boundingBoxes;
	}

	public String name() {
		return name;
	}

	public Map<String, String> properties() {
		return properties;
	}

	public List<BoundingBox> boundingBoxes() {
		return boundingBoxes;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + properties.hashCode();
		result = 31 * result + boundingBoxes.hashCode();
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
		Block other = (Block) obj;
		return name.equals(other.name) &&
			properties.equals(other.properties) &&
			boundingBoxes.equals(other.boundingBoxes);
	}

	@Override
	public String toString() {
		return "Block{name='" + name + "', boundingBoxes=" + boundingBoxes + "}";
	}
}
