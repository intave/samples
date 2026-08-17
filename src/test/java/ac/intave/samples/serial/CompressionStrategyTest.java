package ac.intave.samples.serial;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompressionStrategyTest {
	private static final String SAMPLE = "a1398896-3de1-4427-bebd-9df51d983528.sample";

	@Test
	void comparesNestedIndependentFramesWithOneCombinedZstdFrame() throws IOException {
		byte[] sample = readSample();

		byte[] firstFrame = zstd(sample);
		byte[] secondFrame = zstd(sample);
		byte[] concatenatedFrames = concatenate(firstFrame, secondFrame);
		byte[] gzipOfIndependentFrames = gzipBest(concatenatedFrames);

		byte[] concatenatedSamples = concatenate(sample, sample);
		byte[] zstdOfCombinedSamples = zstd(concatenatedSamples);

		assertArrayEquals(
			concatenatedFrames,
			readAll(new GZIPInputStream(new ByteArrayInputStream(gzipOfIndependentFrames)))
		);
		assertArrayEquals(sample, unzstd(firstFrame));
		assertArrayEquals(sample, unzstd(secondFrame));
		assertArrayEquals(concatenatedSamples, unzstd(zstdOfCombinedSamples));

		long savedBytes = gzipOfIndependentFrames.length - zstdOfCombinedSamples.length;
		double savedPercent = savedBytes * 100.0 / gzipOfIndependentFrames.length;
		long gzipSavedBytes = concatenatedFrames.length - gzipOfIndependentFrames.length;
		double gzipSavedPercent = gzipSavedBytes * 100.0 / concatenatedFrames.length;
		String report = String.format(
			"Raw sample: %,d bytes%n"
				+ "Raw samples combined: %,d bytes%n"
				+ "Zstd level: %d%n"
				+ "zstd(sample) + zstd(sample): %,d bytes%n"
				+ "gzip-9(zstd(sample) + zstd(sample)): %,d bytes%n"
				+ "Outer Gzip saves: %,d bytes (%.3f%%)%n"
				+ "zstd(sample + sample): %,d bytes%n"
				+ "Combined Zstd saves: %,d bytes (%.3f%%)%n",
			sample.length,
			concatenatedSamples.length,
			ZstdStreams.COMPRESSION_LEVEL,
			concatenatedFrames.length,
			gzipOfIndependentFrames.length,
			gzipSavedBytes,
			gzipSavedPercent,
			zstdOfCombinedSamples.length,
			savedBytes,
			savedPercent
		);
		Path reportPath = Paths.get(
			"build", "reports", "compression-strategy.txt"
		).toAbsolutePath();
		Files.createDirectories(reportPath.getParent());
		Files.write(reportPath, report.getBytes(StandardCharsets.UTF_8));

		assertTrue(
			zstdOfCombinedSamples.length < gzipOfIndependentFrames.length,
			"Expected one combined Zstd stream to be smaller.\n" + report
		);
	}

	private static byte[] readSample() throws IOException {
		try (InputStream stream = Objects.requireNonNull(
			CompressionStrategyTest.class.getResourceAsStream("/" + SAMPLE)
		)) {
			return readAll(stream);
		}
	}

	private static byte[] zstd(byte[] input) throws IOException {
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		try (java.io.OutputStream stream = ZstdStreams.compressionStream(compressed)) {
			stream.write(input);
		}
		return compressed.toByteArray();
	}

	private static byte[] unzstd(byte[] input) throws IOException {
		try (InputStream stream = ZstdStreams.decompressionStream(new ByteArrayInputStream(input))) {
			return readAll(stream);
		}
	}

	private static byte[] gzipBest(byte[] input) throws IOException {
		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		try (BestCompressionGzipOutputStream stream = new BestCompressionGzipOutputStream(compressed)) {
			stream.write(input);
		}
		return compressed.toByteArray();
	}

	private static byte[] concatenate(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private static byte[] readAll(InputStream stream) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int count;
		while ((count = stream.read(buffer)) != -1) {
			output.write(buffer, 0, count);
		}
		return output.toByteArray();
	}

	private static final class BestCompressionGzipOutputStream extends GZIPOutputStream {
		private BestCompressionGzipOutputStream(ByteArrayOutputStream output) throws IOException {
			super(output);
			def.setLevel(Deflater.BEST_COMPRESSION);
		}
	}
}
