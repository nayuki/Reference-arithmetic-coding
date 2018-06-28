/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

import java.util.Objects;


/**
 * A mutable table of symbol frequencies. The number of symbols cannot be changed
 * after construction. The current algorithm for calculating cumulative frequencies
 * takes linear time, but there exist faster algorithms such as Fenwick trees.
 */
public final class SimpleFrequencyTable implements FrequencyTable {
	
	/*---- Fields ----*/
	
	// The frequency for each symbol. Its length is at least 1, and each element is non-negative.
	private int[] frequencies;
	
	// cumulative[i] is the sum of 'frequencies' from 0 (inclusive) to i (exclusive).
	// Initialized lazily. When this is not null, the data is valid.
	private int[] cumulative;
	
	// Always equal to the sum of 'frequencies'.
	private int total;
	
	
	
	/*---- Constructors ----*/
	
	/**
	 * Constructs a frequency table from the specified array of symbol frequencies. There must be at least
	 * 1 symbol, no symbol has a negative frequency, and the total must not exceed {@code Integer.MAX_VALUE}.
	 * @param freqs the array of symbol frequencies
	 * @throws NullPointerException if the array is {@code null}
	 * @throws IllegalArgumentException if {@code freqs.length} &lt; 1,
	 * {@code freqs.length} = {@code Integer.MAX_VALUE}, or any element {@code freqs[i]} &lt; 0
	 * @throws ArithmeticException if the total of {@code freqs} exceeds {@code Integer.MAX_VALUE}
	 */
	public SimpleFrequencyTable(int[] freqs) {
		Objects.requireNonNull(freqs);
		if (freqs.length < 1)
			throw new IllegalArgumentException("At least 1 symbol needed");
		if (freqs.length > Integer.MAX_VALUE - 1)
			throw new IllegalArgumentException("Too many symbols");
		
		frequencies = freqs.clone();  // Make copy
		total = 0;
		for (int x : frequencies) {
			if (x < 0)
				throw new IllegalArgumentException("Negative frequency");
			total = checkedAdd(x, total);
		}
		cumulative = null;
	}
	
	
	/**
	 * Constructs a frequency table by copying the specified frequency table.
	 * @param freqs the frequency table to copy
	 * @throws NullPointerException if {@code freqs} is {@code null}
	 * @throws IllegalArgumentException if {@code freqs.getSymbolLimit()} &lt; 1
	 * or any element {@code freqs.get(i)} &lt; 0
	 * @throws ArithmeticException if the total of all {@code freqs} elements exceeds {@code Integer.MAX_VALUE}
	 */
	public SimpleFrequencyTable(FrequencyTable freqs) {
		Objects.requireNonNull(freqs);
		int numSym = freqs.getSymbolLimit();
		if (numSym < 1)
			throw new IllegalArgumentException("At least 1 symbol needed");
		
		frequencies = new int[numSym];
		total = 0;
		for (int i = 0; i < frequencies.length; i++) {
			int x = freqs.get(i);
			if (x < 0)
				throw new IllegalArgumentException("Negative frequency");
			frequencies[i] = x;
			total = checkedAdd(x, total);
		}
		cumulative = null;
	}
	
	
	
	/*---- Methods ----*/
	
	/**
	 * Returns the number of symbols in this frequency table, which is at least 1.
	 * @return the number of symbols in this frequency table
	 */
	public int getSymbolLimit() {
		return frequencies.length;
	}
	
	
	/**
	 * Returns the frequency of the specified symbol. The returned value is at least 0.
	 * @param symbol the symbol to query
	 * @return the frequency of the specified symbol
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public int get(int symbol) {
		checkSymbol(symbol);
		return frequencies[symbol];
	}
	
	
	/**
	 * Sets the frequency of the specified symbol to the specified value. The frequency value
	 * must be at least 0. If an exception is thrown, then the state is left unchanged.
	 * @param symbol the symbol to set
	 * @param freq the frequency value to set
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 * @throws ArithmeticException if this set request would cause the total to exceed {@code Integer.MAX_VALUE}
	 */
	public void set(int symbol, int freq) {
		checkSymbol(symbol);
		if (freq < 0)
			throw new IllegalArgumentException("Negative frequency");
		
		int temp = total - frequencies[symbol];
		if (temp < 0)
			throw new AssertionError();
		total = checkedAdd(temp, freq);
		frequencies[symbol] = freq;
		cumulative = null;
	}
	
	
	/**
	 * Increments the frequency of the specified symbol.
	 * @param symbol the symbol whose frequency to increment
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public void increment(int symbol) {
		checkSymbol(symbol);
		if (frequencies[symbol] == Integer.MAX_VALUE)
			throw new ArithmeticException("Arithmetic overflow");
		total = checkedAdd(total, 1);
		frequencies[symbol]++;
		cumulative = null;
	}
	
	
	/**
	 * Returns the total of all symbol frequencies. The returned value is at
	 * least 0 and is always equal to {@code getHigh(getSymbolLimit() - 1)}.
	 * @return the total of all symbol frequencies
	 */
	public int getTotal() {
		return total;
	}
	
	
	/**
	 * Returns the sum of the frequencies of all the symbols strictly
	 * below the specified symbol value. The returned value is at least 0.
	 * @param symbol the symbol to query
	 * @return the sum of the frequencies of all the symbols below {@code symbol}
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public int getLow(int symbol) {
		checkSymbol(symbol);
		if (cumulative == null)
			initCumulative();
		return cumulative[symbol];
	}
	
	
	/**
	 * Returns the sum of the frequencies of the specified symbol
	 * and all the symbols below. The returned value is at least 0.
	 * @param symbol the symbol to query
	 * @return the sum of the frequencies of {@code symbol} and all symbols below
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public int getHigh(int symbol) {
		checkSymbol(symbol);
		if (cumulative == null)
			initCumulative();
		return cumulative[symbol + 1];
	}
	
	
	// Recomputes the array of cumulative symbol frequencies.
	private void initCumulative() {
		cumulative = new int[frequencies.length + 1];
		int sum = 0;
		for (int i = 0; i < frequencies.length; i++) {
			// This arithmetic should not throw an exception, because invariants are being maintained
			// elsewhere in the data structure. This implementation is just a defensive measure.
			sum = checkedAdd(frequencies[i], sum);
			cumulative[i + 1] = sum;
		}
		if (sum != total)
			throw new AssertionError();
	}
	
	
	// Returns silently if 0 <= symbol < frequencies.length, otherwise throws an exception.
	private void checkSymbol(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
	}
	
	
	/**
	 * Returns a string representation of this frequency table,
	 * useful for debugging only, and the format is subject to change.
	 * @return a string representation of this frequency table
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < frequencies.length; i++)
			sb.append(String.format("%d\t%d%n", i, frequencies[i]));
		return sb.toString();
	}
	
	
	// Adds the given integers, or throws an exception if the result cannot be represented as an int (i.e. overflow).
	private static int checkedAdd(int x, int y) {
		int z = x + y;
		if (y > 0 && z < x || y < 0 && z > x)
			throw new ArithmeticException("Arithmetic overflow");
		else
			return z;
	}
	
}
