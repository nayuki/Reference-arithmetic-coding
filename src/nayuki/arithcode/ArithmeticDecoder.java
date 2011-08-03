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
	
	
	
	public int read(FrequencyTable freq) throws IOException {
		long range = high - low + 1;
		if (code < low || code > high)
			throw new AssertionError("Code out of range");
		
		// Search for symbol
		long offset = code - low;
		
		// Linear search
		int total = freq.getTotal();
		int symbol = -1;
		for (int i = 0; i < freq.getSymbolLimit(); i++) {
			if (offset < freq.getHigh(i) * range / total) {
				symbol = i;
				break;
			}
		}
		if (symbol == -1)
			throw new AssertionError("Symbol not found");
		
		update(freq, symbol);
		return symbol;
	}
	
	
	protected void overflow() throws IOException {
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
