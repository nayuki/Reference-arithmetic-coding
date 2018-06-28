/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

import java.io.IOException;


/**
 * Provides the state and behaviors that arithmetic coding encoders and decoders share.
 * @see ArithmeticEncoder
 * @see ArithmeticDecoder
 */
public abstract class ArithmeticCoderBase {
	
	/*---- Configuration fields ----*/
	
	/**
	 * Number of bits for the 'low' and 'high' state variables. Configurable in the range [1, 62].
	 * <ul>
	 *   <li>For state sizes less than the midpoint of around 32, larger values are generally better -
	 *   they allow a larger maximum frequency total (MAX_TOTAL), and they reduce the approximation
	 *   error inherent in adapting fractions to integers; both effects reduce the data encoding loss
	 *   and asymptotically approach the efficiency of arithmetic coding using exact fractions.</li>
	 *   <li>But for state sizes greater than the midpoint, because intermediate computations are limited
	 *   to the long integer type's 63-bit unsigned precision, larger state sizes will decrease the
	 *   maximum frequency total, which might constrain the user-supplied probability model.</li>
	 *   <li>Therefore STATE_SIZE=32 is recommended as the most versatile setting
	 *   because it maximizes MAX_TOTAL (which ends up being slightly over 2^30).</li>
	 *   <li>Note that STATE_SIZE=62 is legal but useless because it implies MAX_TOTAL=1,
	 *   which means a frequency table can only support one symbol with non-zero frequency.</li>
	 * </ul>
	 */
	protected final long STATE_SIZE;
	
	/** Maximum range (high+1-low) during coding (trivial), which is 2^STATE_SIZE = 1000...000. */
	protected final long MAX_RANGE;
	
	/** Minimum range (high+1-low) during coding (non-trivial), which is 0010...010. */
	protected final long MIN_RANGE;
	
	/** Maximum allowed total from a frequency table at all times during coding. */
	protected final long MAX_TOTAL;
	
	/** Bit mask of STATE_SIZE ones, which is 0111...111. */
	protected final long MASK;
	
	/** The top bit at width STATE_SIZE, which is 0100...000. */
	protected final long TOP_MASK;
	
	/** The second highest bit at width STATE_SIZE, which is 0010...000. This is zero when STATE_SIZE=1. */
	protected final long SECOND_MASK;
	
	
	
	/*---- State fields ----*/
	
	/**
	 * Low end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 0s.
	 */
	protected long low;
	
	/**
	 * High end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 1s.
	 */
	protected long high;
	
	
	
	/*---- Constructor ----*/
	
	/**
	 * Constructs an arithmetic coder, which initializes the code range.
	 * @param stateSize the number of bits for the arithmetic coding range
	 * @throws IllegalArgumentException if stateSize is outside the range [1, 62]
	 */
	public ArithmeticCoderBase(int stateSize) {
		if (stateSize < 1 || stateSize > 62)
			throw new IllegalArgumentException("State size out of range");
		STATE_SIZE = stateSize;
		MAX_RANGE = 1L << STATE_SIZE;
		MIN_RANGE = (MAX_RANGE >>> 2) + 2;
		MAX_TOTAL = Math.min(Long.MAX_VALUE / MAX_RANGE, MIN_RANGE);
		MASK = MAX_RANGE - 1;
		TOP_MASK = MAX_RANGE >>> 1;
		SECOND_MASK = TOP_MASK >>> 1;
		
		low = 0;
		high = MASK;
	}
	
	
	
	/*---- Methods ----*/
	
	/**
	 * Updates the code range (low and high) of this arithmetic coder as a result
	 * of processing the specified symbol with the specified frequency table.
	 * <p>Invariants that are true before and after encoding/decoding each symbol:</p>
	 * <ul>
	 *   <li>0 &le; low &le; code &le; high &lt; 2<sup>STATE_SIZE</sup>. ('code' exists only in the decoder.)
	 *   Therefore these variables are unsigned integers of STATE_SIZE bits.</li>
	 *   <li>(low &lt; 1/2 * 2<sup>STATE_SIZE</sup>) && (high &ge; 1/2 * 2<sup>STATE_SIZE</sup>).
	 *   In other words, they are in different halves of the full range.</li>
	 *   <li>(low &lt; 1/4 * 2<sup>STATE_SIZE</sup>) || (high &ge; 3/4 * 2<sup>STATE_SIZE</sup>).
	 *   In other words, they are not both in the middle two quarters.</li>
	 *   <li>Let range = high &minus; low + 1, then MAX_RANGE/4 &lt; MIN_RANGE &le; range
	 *   &le; MAX_RANGE = 2<sup>STATE_SIZE</sup>. These invariants for 'range' essentially dictate the maximum
	 *   total that the incoming frequency table can have, such that intermediate calculations don't overflow.</li>
	 * </ul>
	 * @param freqs the frequency table to use
	 * @param symbol the symbol that was processed
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
	
	
	/**
	 * Called to handle the situation when the top bit of {@code low} and {@code high} are equal.
	 * @throws IOException if an I/O exception occurred
	 */
	protected abstract void shift() throws IOException;
	
	
	/**
	 * Called to handle the situation when low=01(...) and high=10(...).
	 * @throws IOException if an I/O exception occurred
	 */
	protected abstract void underflow() throws IOException;
	
}
