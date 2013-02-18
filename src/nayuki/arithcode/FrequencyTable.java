package nayuki.arithcode;


/**
 * A table of symbol frequencies. The table holds data for symbols named from 0 (inclusive) to numSymbols (exclusive). Each symbol has a frequency, which is a non-negative integer.
 * <p>Frequency table objects are primarily used for getting cumulative symbol frequencies. These objects can be mutable. The total of all symbol frequencies must not exceed Integer.MAX_VALUE.</p>
 */
public interface FrequencyTable {
	
	/**
	 * Returns the number of symbols in this frequency table.
	 * @return the number of symbols in this frequency table
	 */
	public int getSymbolLimit();
	
	
	/**
	 * Returns the frequency of the specified symbol. The returned value is at least 0. An exception should be thrown if the symbol is out of range.
	 * @param symbol the symbol to query
	 * @return the frequency of the specified symbol
	 */
	public int get(int symbol);
	
	
	/**
	 * Sets the frequency of the specified symbol to the specified value. The frequency value must be at least 0. An exception should be thrown if the symbol is out of range.
	 * @param symbol the symbol to set
	 * @param freq the frequency value to set
	 */
	public void set(int symbol, int freq);
	
	
	/**
	 * Increments the frequency of the specified symbol. An exception should be thrown if the symbol is out of range.
	 * @param symbol the symbol whose frequency to increment
	 */
	public void increment(int symbol);
	
	
	/**
	 * Returns the total of all symbol frequencies. The returned value is at least 0 and is always equal to {@code getHigh(getSymbolLimit() - 1)}.
	 * @return the total of all symbol frequencies
	 */
	public int getTotal();
	
	
	/**
	 * Returns the total of the frequencies of all the symbols below the specified one. The returned value is at least 0. An exception should be thrown if the symbol is out of range.
	 * @param symbol the symbol to query
	 * @return the total of the frequencies of all the symbols below {@code symbol}
	 */
	public int getLow(int symbol);
	
	
	/**
	 * Returns the total of the frequencies of the specified symbol and all the ones below. The returned value is at least 0. An exception should be thrown if the symbol is out of range.
	 * @param symbol the symbol to query
	 * @return the total of the frequencies of {@code symbol} and all the ones below
	 */
	public int getHigh(int symbol);
	
}
