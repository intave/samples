package ac.intave.samples.serial;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class ZstdStreams {
	public static final int COMPRESSION_LEVEL = 16;

	private ZstdStreams() {
	}

	public static OutputStream compressionStream(OutputStream stream) throws IOException {
		return new ZstdOutputStream(stream, COMPRESSION_LEVEL).setCloseFrameOnFlush(false);
	}

	public static InputStream decompressionStream(InputStream stream) throws IOException {
		return new ZstdInputStream(stream);
	}
}
