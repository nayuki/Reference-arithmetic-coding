package nayuki.arithcode;


/**
 * An immutable frequency table where every symbol has the same frequency of 1. Useful as a fallback model when no statistics are available.
 */
public final class FlatFrequencyTable implements FrequencyTable {
	
	// Total number of symbols, which is at least 1.
	private final int numSymbols;
	
	
	
	/**
	 * Creates a flat frequency table with the specified number of symbols.
	 * @param numSyms the number of symbols, which must be at least 1
	 * @throws IllegalArgumentException if {@code numSyms} &le; 0
	 */
	public FlatFrequencyTable(int numSyms) {
		if (numSyms <= 0)
			throw new IllegalArgumentException("Number of symbols must be positive");
		numSymbols = numSyms;
	}
	
	
	
	/**
	 * Returns the number of symbols in this table.
	 * @return the number of symbols in this table
	 */
	public int getSymbolLimit() {
		return numSymbols;
	}
	
	
	/**
	 * Returns the frequency of the specified symbol, which is always 1. If the symbol index is out of bounds, then an exception is thrown instead.
	 * @param symbol the symbol to query
	 * @return the frequency of the symbol, which is 1
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public int get(int symbol) {
		if (symbol < 0 || symbol >= numSymbols)
			throw new IllegalArgumentException("Symbol out of range");
		return 1;
	}
	
	
	/**
	 * Returns the total of all symbol frequencies, which is always equal to the number of symbols in this table.
	 * @return the total of all symbol frequencies, which is {@code getSymbolLimit()}
	 */
	public int getTotal() {
		return numSymbols;
	}
	
	
	/**
	 * Returns the total of the frequencies of all the symbols below the specified one, which is equal to {@code symbol}.
	 * @param symbol the symbol to query
	 * @return the total of the frequencies of all the symbols below the specified one, which is {@code symbol}
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public int getLow(int symbol) {
		if (symbol < 0 || symbol >= numSymbols)
			throw new IllegalArgumentException("Symbol out of range");
		return symbol;
	}
	
	
	/**
	 * Returns the total of the frequencies of the specified symbol and all the ones below, which is equal to {@code symbol} + 1.
	 * @param symbol the symbol to query
	 * @return the total of the frequencies of the specified symbol and all the ones below, which is {@code symbol} + 1
	 * @throws IllegalArgumentException if {@code symbol} &lt; 0 or {@code symbol} &ge; {@code getSymbolLimit()}
	 */
	public int getHigh(int symbol) {
		if (symbol < 0 || symbol >= numSymbols)
			throw new IllegalArgumentException("Symbol out of range");
		return symbol + 1;
	}
	
	
	/**
	 * Returns a string representation of this frequency table. The format is subject to change.
	 * @return a string representation of this frequency table
	 */
	public String toString() {
		return "FlatFrequencyTable=" + numSymbols;
	}
	
	
	/**
	 * Unsupported operation, because this frequency table is immutable.
	 * @param symbol ignored
	 * @param freq ignored
	 * @throws UnsupportedOperationException because this frequency table is immutable
	 */
	public void set(int symbol, int freq) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Unsupported operation, because this frequency table is immutable.
	 * @param symbol ignored
	 * @throws UnsupportedOperationException because this frequency table is immutable
	 */
	public void increment(int symbol) {
		throw new UnsupportedOperationException();
	}
	
}
