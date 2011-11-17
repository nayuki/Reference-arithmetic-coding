package nayuki.arithcode;

import java.io.IOException;


public abstract class ArithmeticCoderBase {
	
	protected final long STATE_SIZE  = 32;
	protected final long MASK        = (1L << (STATE_SIZE - 0)) - 1;  //  111...111
	protected final long TOP_MASK    = (1L << (STATE_SIZE - 1));      //  100...000
	protected final long SECOND_MASK = (1L << (STATE_SIZE - 2));      //  010...000
	protected final long MAX_RANGE   = (1L << (STATE_SIZE - 0));      // 1000...000
	protected final long MIN_RANGE   = (1L << (STATE_SIZE - 2)) + 2;  //   10...010
	protected final long MAX_TOTAL   = Math.min(Long.MAX_VALUE / MAX_RANGE, MIN_RANGE);
	
	protected long low;   // Has an infinite number of trailing 0s
	protected long high;  // Has an infinite number of trailing 1s
	
	
	
	public ArithmeticCoderBase() {
		low = 0;
		high = MASK;
	}
	
	
	
	// Update the range as a result of seeing the symbol, i.e. update low and high.
	protected void update(FrequencyTable freq, int symbol) throws IOException {
		// State check
		long range = high - low + 1;
		if (low >= high || (low & MASK) != low || (high & MASK) != high)
			throw new AssertionError("Low or high out of range");
		if (range < MIN_RANGE || range > MAX_RANGE)
			throw new AssertionError("Range out of range");
		
		// Frequency table usage check
		long total = freq.getTotal();
		long symLow = freq.getLow(symbol);
		long symHigh = freq.getHigh(symbol);
		if (!(total > 0 && 0 <= symLow && symLow <= symHigh && symHigh <= total))
			throw new IllegalArgumentException("Illegal values from frequency table");
		if (symLow == symHigh)
			throw new IllegalArgumentException("Symbol has zero frequency");
		if (total > MAX_TOTAL)
			throw new IllegalArgumentException("Cannot code symbol in range");
		
		// Update range
		long newLow  = low + symLow  * range / total;
		long newHigh = low + symHigh * range / total - 1;
		low = newLow;
		high = newHigh;
		
		// While the highest bits are equal
		while (((low ^ high) & TOP_MASK) == 0) {
			shift();
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
	
	
	// Called when the top bit of low and high are equal.
	protected abstract void shift() throws IOException;
	
	// Called when low=01xxxx and high=10yyyy.
	protected abstract void underflow() throws IOException;
	
}
