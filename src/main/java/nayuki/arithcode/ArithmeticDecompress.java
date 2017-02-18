package nayuki.arithcode;

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
		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);
		
		BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		try {
			FrequencyTable freq = readFrequencies(in);
			decompress(freq, in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	static FrequencyTable readFrequencies(BitInputStream in) throws IOException {
		int[] freqs = new int[257];
		for (int i = 0; i < 256; i++)
			freqs[i] = readInt(in, 32);
		freqs[256] = 1;  // EOF symbol
		return new SimpleFrequencyTable(freqs);
	}
	
	
	static void decompress(FrequencyTable freq, BitInputStream in, OutputStream out) throws IOException {
		ArithmeticDecoder dec = new ArithmeticDecoder(in);
		while (true) {
			int symbol = dec.read(freq);
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
