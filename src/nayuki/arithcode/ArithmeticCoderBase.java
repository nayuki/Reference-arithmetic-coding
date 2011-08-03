package nayuki.arithcode;

import java.io.IOException;


public abstract class ArithmeticCoderBase {
	
	protected final long STATE_SIZE  = 32;
	protected final long MASK        = (1L << STATE_SIZE - 0) - 1;
	protected final long TOP_MASK    = (1L << STATE_SIZE - 1);
	protected final long SECOND_MASK = (1L << STATE_SIZE - 2);
	protected final long MAX_RANGE   = (1L << STATE_SIZE - 0);
	protected final long MIN_RANGE   = (1L << STATE_SIZE - 2) + 2;
	
	protected long low;   // Has an infinite number of trailing zeros
	protected long high;  // Has an infinite number of trailing ones
	
	
	
	public ArithmeticCoderBase() {
		low = 0;
		high = MASK;
	}
	
	
	
	protected void update(FrequencyTable freq, int symbol) throws IOException {
		long range = high - low + 1;
		long total = freq.getTotal();
		long symLow = freq.getLow(symbol);
		long symHigh = freq.getHigh(symbol);
		
		if (symLow == symHigh)
			throw new IllegalArgumentException("Symbol has zero frequency");
		if (low > high || (low & MASK) != low || (high & MASK) != high)
			throw new AssertionError("Low or high out of range");
		if (range < MIN_RANGE || range > MAX_RANGE)
			throw new AssertionError("Range out of range");
		if (total > MIN_RANGE || Long.MAX_VALUE / total < MAX_RANGE)
			throw new AssertionError("Cannot code symbol in range");
		
		// Update range
		long newLow  = low + symLow  * range / total;
		long newHigh = low + symHigh * range / total - 1;
		low = newLow;
		high = newHigh;
		
		// While the highest bits are equal
		while (((low ^ high) & TOP_MASK) == 0) {
			overflow();
			low = (low << 1) & MASK;
			high = ((high << 1) & MASK) | 1;
		}
		
		// While the second highest bit of low is 1 and the second highest bit of high is 0
		while ((low & ~high & SECOND_MASK) != 0) {
			underflow();
			low = (low << 1) & (MASK >>> 1);
			high = ((high << 1) & (MASK >>> 1)) | TOP_MASK | 1;
		}
	}
	
	
	protected abstract void overflow() throws IOException;
	
	protected abstract void underflow() throws IOException;
	
}
