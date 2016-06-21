package nayuki.arithcode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ArithmeticCompress {
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java ArithmeticCompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, compress
		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);
		
		// Read input file once to compute symbol frequencies
		FrequencyTable freq = getFrequencies(inputFile);
		freq.increment(256);  // EOF symbol gets a frequency of 1
		
		// Read input file again, compress with arithmetic coding, and write output file
		InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
		BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		try {
			writeFrequencies(out, freq);
			compress(freq, in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	private static FrequencyTable getFrequencies(File file) throws IOException {
		FrequencyTable freq = new SimpleFrequencyTable(new int[257]);
		InputStream input = new BufferedInputStream(new FileInputStream(file));
		try {
			while (true) {
				int b = input.read();
				if (b == -1)
					break;
				freq.increment(b);
			}
		} finally {
			input.close();
		}
		return freq;
	}
	
	
	static void writeFrequencies(BitOutputStream out, FrequencyTable freq) throws IOException {
		for (int i = 0; i < 256; i++)
			writeInt(out, 32, freq.get(i));
	}
	
	
	static void compress(FrequencyTable freq, InputStream in, BitOutputStream out) throws IOException {
		ArithmeticEncoder enc = new ArithmeticEncoder(out);
		while (true) {
			int b = in.read();
			if (b == -1)
				break;
			enc.write(freq, b);
		}
		enc.write(freq, 256);  // EOF
		enc.finish();
	}
	
	
	private static void writeInt(BitOutputStream out, int numBits, int value) throws IOException {
		if (numBits < 0 || numBits > 32)
			throw new IllegalArgumentException();
		
		for (int i = 0; i < numBits; i++)
			out.write(value >>> i & 1);  // Little endian
	}
	
}
