package ac.intave.samples.event;

import java.util.Collection;
import java.util.List;

/**
 * An event whose ordered values can be written as several equivalent events.
 * Implementations opt in only when processing the chunks in order preserves the
 * meaning of the original event.
 *
 * @param <T> value type partitioned across chunks
 */
public interface ChunkableEvent<T> {
	/**
	 * Identifies the collection in the serialized event.
	 *
	 * @return serialized field containing the values
	 */
	String chunkFieldName();

	/**
	 * Supplies the values that may be partitioned.
	 *
	 * @return non-null values to preserve in order across all chunks
	 */
	List<T> chunkValues();

	/**
	 * Creates an event template for a chunk.
	 *
	 * @param values values for the new event
	 * @return a copy containing only the supplied values
	 */
	Event chunkWith(Collection<T> values);

	/**
	 * Quickly determines whether the serialized event could exceed the supplied
	 * character limit. False must guarantee that the event fits.
	 *
	 * @param characterLimit maximum characters accepted by the reader
	 * @return true when the writer should use the chunking path
	 */
	boolean couldExceedCharacterLimit(int characterLimit);
}
