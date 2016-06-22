/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

import java.io.IOException;


// A model of the state and behaviors that arithmetic coding encoders and decoders share.
public abstract class ArithmeticCoderBase {
	
	/* Configuration */
	
	/**
	 * Number of bits for 'low' and 'high'. Configurable in the range [1, 62] (but possibly more restricted).
	 */
	protected final long STATE_SIZE = 32;
	
	
	/* Constants */
	
	/**
	 * Maximum range during coding (trivial), i.e. 1000...000.
	 */
	protected final long MAX_RANGE = 1L << STATE_SIZE;
	
	/**
	 * Minimum range during coding (non-trivial), i.e. 010...010.
	 */
	protected final long MIN_RANGE = (1L << (STATE_SIZE - 2)) + 2;
	
	/**
	 * Maximum allowed total frequency at all times during coding.
	 */
	protected final long MAX_TOTAL = Math.min(Long.MAX_VALUE / MAX_RANGE, MIN_RANGE);
	
	/**
	 * Mask of STATE_SIZE ones, i.e. 111...111.
	 */
	protected final long MASK = MAX_RANGE - 1;
	
	/**
	 * Mask of the top bit at width STATE_SIZE, i.e. 100...000.
	 */
	protected final long TOP_MASK = MAX_RANGE >>> 1;
	
	/**
	 * Mask of the second highest bit at width STATE_SIZE, i.e. 010...000.
	 */
	protected final long SECOND_MASK = TOP_MASK >>> 1;
	
	
	/* Fields */
	
	/**
	 * Low end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 0s.
	 */
	protected long low;
	
	/**
	 * High end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 1s.
	 */
	protected long high;
	
	
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
	protected void update(CheckedFrequencyTable freqs, int symbol) throws IOException {
		// State check
		if (low >= high || (low & MASK) != low || (high & MASK) != high)
			throw new AssertionError("Low or high out of range");
		long range = high - low + 1;
		if (range < MIN_RANGE || range > MAX_RANGE)
			throw new AssertionError("Range out of range");
		
		// Frequency table values check
		long total = freqs.getTotal();
		long symLow = freqs.getLow(symbol);
		long symHigh = freqs.getHigh(symbol);
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
