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
	
	// Must be at least -1. Warning: Exponential memory usage at O(257^n).
	static final int CONTEXT_ORDER = 3;
	
	
	public static void main(String[] args) throws IOException {
		// Handle command line arguments
		if (args.length != 2) {
			System.err.println("Usage: java PpmCompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		File inputFile  = new File(args[0]);
		File outputFile = new File(args[1]);
		
		try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile))) {
			try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
				compress(in, out);
			}
		}
	}
	
	
	// To allow unit testing, this method is package-private instead of private.
	static void compress(InputStream in, BitOutputStream out) throws IOException {
		// Set up encoder and model
		ArithmeticEncoder enc = new ArithmeticEncoder(out);
		PpmContext rootCtx = new PpmContext(257, CONTEXT_ORDER >= 1);
		rootCtx.frequencies.increment(256);
		int[] history = new int[0];
		
		while (true) {
			// Read and encode one byte
			int symbol = in.read();
			if (symbol == -1)
				break;
			
			encodeSymbol(rootCtx, history, symbol, enc);
			incrementContexts(history, symbol, rootCtx);
			
			// Append current symbol or shift back by one
			if (CONTEXT_ORDER >= 1) {
				if (history.length < CONTEXT_ORDER)
					history = Arrays.copyOf(history, history.length + 1);
				else
					System.arraycopy(history, 1, history, 0, history.length - 1);
				history[history.length - 1] = symbol;
			}
		}
		
		encodeSymbol(rootCtx, history, 256, enc);  // EOF
		enc.finish();  // Flush remaining code bits
	}
	
	
	private static void encodeSymbol(PpmContext rootCtx, int[] history, int symbol, ArithmeticEncoder enc) throws IOException {
		outer:
		for (int order = Math.min(history.length, CONTEXT_ORDER); order >= -1; order--) {
			if (order >= 0) {
				PpmContext ctx = rootCtx;
				for (int i = history.length - order; i < history.length; i++) {
					if (ctx.subcontexts == null)
						throw new AssertionError();
					ctx = ctx.subcontexts[history[i]];
					if (ctx == null)
						continue outer;
				}
				if (symbol != 256 && ctx.frequencies.get(symbol) > 0) {
					enc.write(ctx.frequencies, symbol);
					break;
				} else {
					enc.write(ctx.frequencies, 256);  // Context escape symbol
					// Continue decrementing the order
				}
			} else if (order == -1)
				enc.write(ORDER_MINUS1_FREQS, symbol);
			else
				throw new AssertionError();
		}
	}
	
	
	/* Members shared by compressor and decompressor */
	
	static void incrementContexts(int[] history, int symbol, PpmContext rootCtx) {
		for (int order = Math.min(history.length, CONTEXT_ORDER); order >= 0; order--) {
			PpmContext ctx = rootCtx;
			for (int i = history.length - order, depth = 0; i < history.length; i++, depth++) {
				if (ctx.subcontexts == null)
					throw new AssertionError();
				int sym = history[i];
				if (ctx.subcontexts[sym] == null) {
					ctx.subcontexts[sym] = new PpmContext(257, depth + 1 < CONTEXT_ORDER);
					ctx.subcontexts[sym].frequencies.increment(256);
				}
				ctx = ctx.subcontexts[sym];
			}
			ctx.frequencies.increment(symbol);
		}
	}
	
	
	static final FrequencyTable ORDER_MINUS1_FREQS = new FlatFrequencyTable(257);
	
	
	
	static final class PpmContext {
		
		public final FrequencyTable frequencies;
		
		public final PpmContext[] subcontexts;
		
		
		public PpmContext(int symbols, boolean hasSubctx) {
			frequencies = new SimpleFrequencyTable(new int[symbols]);
			if (hasSubctx)
				subcontexts = new PpmContext[symbols];
			else
				subcontexts = null;
		}
		
	}
	
}
