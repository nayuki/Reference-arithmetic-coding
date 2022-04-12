import inputstreams.ByteInputStreamFactory;
import inputstreams.FileInputStreamFactory;
import inputstreams.InputStreamFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class ByteTransformer {
	public void commandLineMain(String[] args) throws IOException {
		if (args.length != 2) {
			System.err.printf("Usage: java %s InputFile OutputFile%n", this.getClass().getSimpleName());
			System.exit(1);
			return;
		}

		transformFile(args[0], args[1]);
	}

	public abstract void transformStream(InputStreamFactory inputStreamFactory, OutputStream outputStream) throws IOException;

	public void transformFile(String inputFile, String outputFile) throws IOException {
		InputStreamFactory factory = new FileInputStreamFactory(inputFile);
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		transformStream(factory, out);
	}

	public byte[] transformByteArray(byte[] b) throws IOException {
		InputStreamFactory factory = new ByteInputStreamFactory(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		transformStream(factory, out);
		return out.toByteArray();
	}
}
