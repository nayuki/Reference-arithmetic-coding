package nayuki.arithcode;


/**
 * A mutable table of symbol frequencies. The number of symbols cannot be changed after construction.
 * The current algorithm for calculating cumulative frequencies takes linear time, but there exist faster algorithms.
 */
public final class SimpleFrequencyTable implements FrequencyTable {
	
	// The frequency for each symbol.
	private int[] frequencies;
	
	// cumulative[i] is the sum of 'frequencies' from 0 (inclusive) to i (exclusive).
	// Initialized lazily. When this is not null, the data is valid.
	private int[] cumulative;
	
	// Equal to the sum of 'frequencies'.
	private int total;
	
	
	
	/**
	 * Creates a frequency table from the specified array of symbol frequencies. There must be at least 1 symbol, and no symbol has a negative frequency.
	 * @param freqs the array of symbol frequencies
	 * @throws NullPointerException if {@code freqs} is {@code null}
	 * @throws IllegalArgumentException if {@code freqs.length} &lt; 1 or any element {@code freqs[i]} &lt; 0
	 * @throws ArithmeticException if the total of {@code freqs} exceeds {@code Integer.MAX_VALUE}
	 */
	public SimpleFrequencyTable(int[] freqs) {
		if (freqs == null)
			throw new NullPointerException("Argument is null");
		if (freqs.length == 0)
			throw new IllegalArgumentException("At least 1 symbol needed");
		
		frequencies = freqs.clone();  // Defensive copy
		total = 0;
		for (int x : frequencies) {
			if (x < 0)
				throw new IllegalArgumentException("Negative frequency");
			total = checkedAdd(x, total);
		}
		cumulative = null;
	}
	
	
	/**
	 * Creates a frequency table by copying the specified frequency table.
	 * @param freqTab the frequency table to copy
	 * @throws NullPointerException if {@code freqTab} is {@code null}
	 * @throws IllegalArgumentException if {@code freqTab.getSymbolLimit()} &lt; 1 or any element {@code freqTab.get(i)} &lt; 0
	 * @throws ArithmeticException if the total of all {@code freqTab} elements exceeds {@code Integer.MAX_VALUE}
	 */
	public SimpleFrequencyTable(FrequencyTable freqTab) {
		if (freqTab == null)
			throw new NullPointerException("Argument is null");
		int numSym = freqTab.getSymbolLimit();
		if (numSym < 0)
			throw new IllegalArgumentException("At least 1 symbol needed");
		frequencies = new int[numSym];
		total = 0;
		for (int i = 0; i < frequencies.length; i++) {
			int x = freqTab.get(i);
			if (x < 0)
				throw new IllegalArgumentException("Negative frequency");
			frequencies[i] = x;
			total = checkedAdd(x, total);
		}
		cumulative = null;
	}
	
	
	
	/**
	 * Returns the number of symbols in this frequency table.
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
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		return frequencies[symbol];
	}
	
	
	/**
	 * Sets the frequency of the specified symbol to the specified value. The frequency value must be at least 0.
	 * @param symbol the symbol to set
	 * @param freq the frequency value to set
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public void set(int symbol, int freq) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		if (freq < 0)
			throw new IllegalArgumentException("Negative frequency");
		
		total = checkedAdd(total - frequencies[symbol], freq);
		frequencies[symbol] = freq;
		cumulative = null;
	}
	
	
	/**
	 * Increments the frequency of the specified symbol. An exception should be thrown if the symbol is out of range.
	 * @param symbol the symbol whose frequency to increment
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public void increment(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		if (frequencies[symbol] == Integer.MAX_VALUE)
			throw new RuntimeException("Arithmetic overflow");
		
		total = checkedAdd(total, 1);
		frequencies[symbol]++;
		cumulative = null;
	}
	
	
	/**
	 * Returns the total of all symbol frequencies. The returned value is at least 0 and is always equal to {@code getHigh(getSymbolLimit() - 1)}.
	 * @return the total of all symbol frequencies
	 */
	public int getTotal() {
		return total;
	}
	
	
	/**
	 * Returns the total of the frequencies of all the symbols below the specified one. The returned value is at least 0. An exception should be thrown if the symbol is out of range.
	 * @param symbol the symbol to query
	 * @return the total of the frequencies of all the symbols below {@code symbol}
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public int getLow(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		
		if (cumulative == null)
			initCumulative();
		return cumulative[symbol];
	}
	
	
	/**
	 * Returns the total of the frequencies of the specified symbol and all the ones below. The returned value is at least 0. An exception should be thrown if the symbol is out of range.
	 * @param symbol the symbol to query
	 * @return the total of the frequencies of {@code symbol} and all the ones below
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public int getHigh(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		
		if (cumulative == null)
			initCumulative();
		return cumulative[symbol + 1];
	}
	
	
	private void initCumulative() {
		cumulative = new int[frequencies.length + 1];
		int sum = 0;
		for (int i = 0; i < frequencies.length; i++) {
			sum = checkedAdd(frequencies[i], sum);
			cumulative[i + 1] = sum;
		}
		if (sum != total)
			throw new AssertionError();
	}
	
	
	/**
	 * Returns a string representation of this frequency table. The current format shows all the symbols and frequencies. The format is subject to change.
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
