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


public class AdaptiveArithmeticDecompress {
	
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
			decompress(in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	// To allow unit testing, this method is package-private instead of private.
	static void decompress(BitInputStream in, OutputStream out) throws IOException {
		FrequencyTable freqs = new SimpleFrequencyTable(new FlatFrequencyTable(257));  // Initialize with all symbol frequencies at 1
		ArithmeticDecoder dec = new ArithmeticDecoder(in);
		while (true) {
			int symbol = dec.read(freqs);
			if (symbol == 256)  // EOF symbol
				break;
			out.write(symbol);
			freqs.increment(symbol);
		}
	}
	
}
