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
import java.io.OutputStream;


public class ArithmeticDecompress {
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java ArithmeticDecompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, decompress
		File inputFile  = new File(args[0]);
		File outputFile = new File(args[1]);
		
		BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			FrequencyTable freqs = readFrequencies(in);
			decompress(freqs, in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	// To allow unit testing, this method is package-private instead of private.
	static FrequencyTable readFrequencies(BitInputStream in) throws IOException {
		int[] freqs = new int[257];
		for (int i = 0; i < 256; i++)
			freqs[i] = readInt(in, 32);
		freqs[256] = 1;  // EOF symbol
		return new SimpleFrequencyTable(freqs);
	}
	
	
	// To allow unit testing, this method is package-private instead of private.
	static void decompress(FrequencyTable freqs, BitInputStream in, OutputStream out) throws IOException {
		ArithmeticDecoder dec = new ArithmeticDecoder(in);
		while (true) {
			int symbol = dec.read(freqs);
			if (symbol == 256)  // EOF symbol
				break;
			out.write(symbol);
		}
	}
	
	
	private static int readInt(BitInputStream in, int numBits) throws IOException {
		if (numBits < 0 || numBits > 32)
			throw new IllegalArgumentException();
		
		int result = 0;
		for (int i = 0; i < numBits; i++)
			result |= in.readNoEof() << i;  // Little endian
		return result;
	}
	
}
