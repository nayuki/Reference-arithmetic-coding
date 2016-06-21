/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ArithmeticCompress {
	
	public static void main(String[] args) throws IOException {
		// Handle command line arguments
		if (args.length != 2) {
			System.err.println("Usage: java ArithmeticCompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		File inputFile  = new File(args[0]);
		File outputFile = new File(args[1]);
		
		// Read input file once to compute symbol frequencies
		FrequencyTable freqs = getFrequencies(inputFile);
		freqs.increment(256);  // EOF symbol gets a frequency of 1
		
		// Read input file again, compress with arithmetic coding, and write output file
		InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
		BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		try {
			writeFrequencies(out, freqs);
			compress(freqs, in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	private static FrequencyTable getFrequencies(File file) throws IOException {
		FrequencyTable freqs = new SimpleFrequencyTable(new int[257]);
		InputStream input = new BufferedInputStream(new FileInputStream(file));
		try {
			while (true) {
				int symbol = input.read();
				if (symbol == -1)
					break;
				freqs.increment(symbol);
			}
		} finally {
			input.close();
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
		ArithmeticEncoder enc = new ArithmeticEncoder(out);
		while (true) {
			int b = in.read();
			if (b == -1)
				break;
			enc.write(freqs, b);
		}
		enc.write(freqs, 256);  // EOF
		enc.finish();
	}
	
	
	private static void writeInt(BitOutputStream out, int numBits, int value) throws IOException {
		if (numBits < 0 || numBits > 32)
			throw new IllegalArgumentException();
		
		for (int i = 0; i < numBits; i++)
			out.write(value >>> i & 1);  // Little endian
	}
	
}
