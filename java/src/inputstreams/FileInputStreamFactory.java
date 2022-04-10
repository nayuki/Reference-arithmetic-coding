package inputstreams;

import java.io.*;

public class FileInputStreamFactory implements InputStreamFactory {

	private final String path;

	public FileInputStreamFactory(String path) {
		this.path = path;
	}

	@Override
	public InputStream getStream() throws IOException {
		return new BufferedInputStream(new FileInputStream(path));
	}
}
