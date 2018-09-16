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
import java.util.Arrays;


/**
 * Compression application using prediction by partial matching (PPM) with arithmetic coding.
 * <p>Usage: java PpmCompress InputFile OutputFile</p>
 * <p>Then use the corresponding "PpmDecompress" application to recreate the original input file.</p>
 * <p>Note that both the compressor and decompressor need to use the same PPM context modeling logic.
 * The PPM algorithm can be thought of as a powerful generalization of adaptive arithmetic coding.</p>
 */
public final class PpmCompress {
	
	// Must be at least -1 and match PpmDecompress. Warning: Exponential memory usage at O(257^n).
	private static final int MODEL_ORDER = 3;
	
	
	public static void main(String[] args) throws IOException {
		// Handle command line arguments
		if (args.length != 2) {
			System.err.println("Usage: java PpmCompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		File inputFile  = new File(args[0]);
		File outputFile = new File(args[1]);
		
		// Perform file compression
		try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
				BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
			compress(in, out);
		}
	}
	
	
	// To allow unit testing, this method is package-private instead of private.
	static void compress(InputStream in, BitOutputStream out) throws IOException {
		// Set up encoder and model. In this PPM model, symbol 256 represents EOF;
		// its frequency is 1 in the order -1 context but its frequency
		// is 0 in all other contexts (which have non-negative order).
		ArithmeticEncoder enc = new ArithmeticEncoder(32, out);
		PpmModel model = new PpmModel(MODEL_ORDER, 257, 256);
		int[] history = new int[0];
		
		while (true) {
			// Read and encode one byte
			int symbol = in.read();
			if (symbol == -1)
				break;
			encodeSymbol(model, history, symbol, enc);
			model.incrementContexts(history, symbol);
			
			if (model.modelOrder >= 1) {
				// Prepend current symbol, dropping oldest symbol if necessary
				if (history.length < model.modelOrder)
					history = Arrays.copyOf(history, history.length + 1);
				System.arraycopy(history, 0, history, 1, history.length - 1);
				history[0] = symbol;
			}
		}
		
		encodeSymbol(model, history, 256, enc);  // EOF
		enc.finish();  // Flush remaining code bits
	}
	
	
	private static void encodeSymbol(PpmModel model, int[] history, int symbol, ArithmeticEncoder enc) throws IOException {
		// Try to use highest order context that exists based on the history suffix, such
		// that the next symbol has non-zero frequency. When symbol 256 is produced at a context
		// at any non-negative order, it means "escape to the next lower order with non-empty
		// context". When symbol 256 is produced at the order -1 context, it means "EOF".
		outer:
		for (int order = history.length; order >= 0; order--) {
			PpmModel.Context ctx = model.rootContext;
			for (int i = 0; i < order; i++) {
				if (ctx.subcontexts == null)
					throw new AssertionError();
				ctx = ctx.subcontexts[history[i]];
				if (ctx == null)
					continue outer;
			}
			if (symbol != 256 && ctx.frequencies.get(symbol) > 0) {
				enc.write(ctx.frequencies, symbol);
				return;
			}
			// Else write context escape symbol and continue decrementing the order
			enc.write(ctx.frequencies, 256);
		}
		// Logic for order = -1
		enc.write(model.orderMinus1Freqs, symbol);
	}
	
}
