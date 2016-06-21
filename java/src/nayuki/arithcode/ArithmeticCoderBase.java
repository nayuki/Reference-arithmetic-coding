package nayuki.arithcode;

import java.io.IOException;


// A model of the state and behaviors that arithmetic coding encoders and decoders share.
public abstract class ArithmeticCoderBase {
	
	// Configuration and constants
	protected final long STATE_SIZE  = 32;  // Number of bits for 'low' and 'high'. Must be in the range [1, 62] (and possibly more restricted).
	protected final long MASK        = (1L << (STATE_SIZE - 0)) - 1;  //  111...111, all ones
	protected final long TOP_MASK    = (1L << (STATE_SIZE - 1));      //  100...000, the top bit
	protected final long SECOND_MASK = (1L << (STATE_SIZE - 2));      //  010...000, the next highest bit
	protected final long MAX_RANGE   = (1L << (STATE_SIZE - 0));      // 1000...000, maximum range during coding (trivial)
	protected final long MIN_RANGE   = (1L << (STATE_SIZE - 2)) + 2;  //   10...010, minimum range during coding (non-trivial)
	protected final long MAX_TOTAL   = Math.min(Long.MAX_VALUE / MAX_RANGE, MIN_RANGE);  // Maximum allowed total frequency at all times during coding
	
	// The arithmetic coder's current range
	protected long low;   // Conceptually has an infinite number of trailing 0s
	protected long high;  // Conceptually has an infinite number of trailing 1s
	
	
	
	public ArithmeticCoderBase() {
		low = 0;
		high = MASK;
	}
	
	
	
	/* 
	 * Updates the range as a result of seeing the given symbol - i.e. update low and high.
	 * 
	 * Invariants that are true before and after encoding/decoding each symbol:
	 * - 0 <= low <= code <= high < 2^STATE_SIZE. ('code' exists only in the decoder.) Therefore these variables are unsigned integers of STATE_SIZE bits.
	 * - low < 1/2 * 2^STATE_SIZE && high >= 1/2 * 2^STATE_SIZE. In other words, they are in different halves of the full range.
	 * - low < 1/4 * 2^STATE_SIZE || high >= 3/4 * 2^STATE_SIZE. In other words, they are not both in the middle two quarters.
	 * - Let range = high - low + 1, then MIN_RANGE <= range <= MAX_RANGE = 2^STATE_SIZE. In particular, range > MAX_RANGE/4.
	 * The invariants for 'range' essentially dictate the maximum total that the incoming frequency table can have, such that intermediate calculations don't overflow.
	 */
	protected void update(CheckedFrequencyTable freq, int symbol) throws IOException {
		// State check
		if (low >= high || (low & MASK) != low || (high & MASK) != high)
			throw new AssertionError("Low or high out of range");
		long range = high - low + 1;
		if (range < MIN_RANGE || range > MAX_RANGE)
			throw new AssertionError("Range out of range");
		
		// Frequency table values check
		long total = freq.getTotal();
		long symLow = freq.getLow(symbol);
		long symHigh = freq.getHigh(symbol);
		if (symLow == symHigh)
			throw new IllegalArgumentException("Symbol has zero frequency");
		if (total > MAX_TOTAL)
			throw new IllegalArgumentException("Cannot code symbol because total is too large");
		
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
