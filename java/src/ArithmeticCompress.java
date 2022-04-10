/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

import inputstreams.InputStreamFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Compression application using static arithmetic coding.
 * <p>Usage: java ArithmeticCompress InputFile OutputFile</p>
 * <p>Then use the corresponding "ArithmeticDecompress" application to recreate the original input file.</p>
 * <p>Note that the application uses an alphabet of 257 symbols - 256 symbols for the byte
 * values and 1 symbol for the EOF marker. The compressed file format starts with a list
 * of 256 symbol frequencies, and then followed by the arithmetic-coded data.</p>
 */
public class ArithmeticCompress extends ByteTransformer {
	
	public static void main(String[] args) throws IOException {
		new ArithmeticCompress().commandLineMain(args);
	}
	
	
	@Override
	public void transformStream(InputStreamFactory inputStreamFactory, OutputStream outputStream) throws IOException {
		// Read input file once to compute symbol frequencies
		FrequencyTable freqs = getFrequencies(inputStreamFactory.getStream());
		freqs.increment(256);  // EOF symbol gets a frequency of 1
		
		// Read input file again, compress with arithmetic coding, and write output file
		try (InputStream in = inputStreamFactory.getStream();
				BitOutputStream out = new BitOutputStream(outputStream)) {
			writeFrequencies(out, freqs);
			compress(freqs, in, out);
		}
	}
	
	
	// Returns a frequency table based on the bytes in the given InputStream.
	// Also contains an extra entry for symbol 256, whose frequency is set to 0.
	private static FrequencyTable getFrequencies(InputStream input) throws IOException {
		FrequencyTable freqs = new SimpleFrequencyTable(new int[257]);
		try (input) {
			while (true) {
				int b = input.read();
				if (b == -1)
					break;
				freqs.increment(b);
			}
		}
		return freqs;
	}
	
	
	// To allow unit testing, this method is package-private instead of private.
	static void writeFrequencies(BitOutputStream out, FrequencyTable freqs) throws IOException {
		for (int i = 0; i < 256; i++)
			writeInt(out, 32, freqs.get(i));
	}
	
	
	// To allow unit testing, this method is package-private instead of private.
	static void compress(FrequencyTable freqs, InputStream in, BitOutputStream out) throws IOException {
		ArithmeticEncoder enc = new ArithmeticEncoder(32, out);
		while (true) {
			int symbol = in.read();
			if (symbol == -1)
				break;
			enc.write(freqs, symbol);
		}
		enc.write(freqs, 256);  // EOF
		enc.finish();  // Flush remaining code bits
	}
	
	
	// Writes an unsigned integer of the given bit width to the given stream.
	private static void writeInt(BitOutputStream out, int numBits, int value) throws IOException {
		if (numBits < 0 || numBits > 32)
			throw new IllegalArgumentException();
		
		for (int i = numBits - 1; i >= 0; i--)
			out.write((value >>> i) & 1);  // Big endian
	}
	
}
