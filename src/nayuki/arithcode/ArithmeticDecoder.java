package nayuki.arithcode;

import java.io.IOException;


public final class ArithmeticDecoder extends ArithmeticCoderBase {
	
	private BitInputStream input;
	
	private long code;
	
	
	
	public ArithmeticDecoder(BitInputStream in) throws IOException {
		super();
		if (in == null)
			throw new NullPointerException();
		input = in;
		code = 0;
		for (int i = 0; i < STATE_SIZE; i++)
			code = code << 1 | readCodeBit();
	}
	
	
	
	// Decodes and returns a symbol.
	public int read(FrequencyTable freq) throws IOException {
		long range = high - low + 1;
		if (code < low || code > high)
			throw new AssertionError("Code out of range");
		
		// Translate scales
		long offset = code - low;
		long value = ((offset + 1) * freq.getTotal() - 1) / range;
		if (value * range / freq.getTotal() > offset)
			throw new AssertionError();
		
		// A kind of binary search
		int start = 0;
		int end = freq.getSymbolLimit();
		while (end - start > 1) {
			int middle = (start + end) >>> 1;
			if (freq.getLow(middle) > value)
				end = middle;
			else
				start = middle;
		}
		int symbol = start;
		
		if (freq.getLow(symbol) * range / freq.getTotal() > offset || freq.getHigh(symbol) * range / freq.getTotal() <= offset)
			throw new AssertionError();
		
		update(freq, symbol);
		return symbol;
	}
	
	
	protected void shift() throws IOException {
		code = ((code << 1) & MASK) | readCodeBit();
	}
	
	
	protected void underflow() throws IOException {
		code = (code & TOP_MASK) | ((code << 1) & (MASK >>> 1)) | readCodeBit();
	}
	
	
	private int readCodeBit() throws IOException {
		int temp = input.read();
		if (temp != -1)
			return temp;
		else  // Treat end of stream as an infinite number of trailing zeros
			return 0;
	}
	
}
