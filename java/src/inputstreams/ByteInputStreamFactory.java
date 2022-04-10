package inputstreams;

import java.io.*;

public class ByteInputStreamFactory implements InputStreamFactory {

	private final byte[] bytes;

	public ByteInputStreamFactory(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public InputStream getStream() {
		return new ByteArrayInputStream(bytes);
	}
}
