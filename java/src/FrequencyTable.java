/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */


/**
 * A table of symbol frequencies. The table holds data for symbols numbered from 0
 * to getSymbolLimit()&minus;1. Each symbol has a frequency, which is a non-negative integer.
 * <p>Frequency table objects are primarily used for getting cumulative symbol
 * frequencies. These objects can be mutable depending on the implementation.
 * The total of all symbol frequencies must not exceed Integer.MAX_VALUE.</p>
 */
public interface FrequencyTable {
	
	/**
	 * Returns the number of symbols in this frequency table, which is a positive number.
	 * @return the number of symbols in this frequency table
	 */
	public int getSymbolLimit();
	
	
	/**
	 * Returns the frequency of the specified symbol. The returned value is at least 0.
	 * @param symbol the symbol to query
	 * @return the frequency of the symbol
	 * @throws IllegalArgumentException if the symbol is out of range
	 */
	public int get(int symbol);
	
	
	/**
	 * Sets the frequency of the specified symbol to the specified value.
	 * The frequency value must be at least 0.
	 * @param symbol the symbol to set
	 * @param freq the frequency value to set
	 * @throws IllegalArgumentException if the frequency is negative or the symbol is out of range
	 * @throws ArithmeticException if an arithmetic overflow occurs
	 */
	public void set(int symbol, int freq);
	
	
	/**
	 * Increments the frequency of the specified symbol.
	 * @param symbol the symbol whose frequency to increment
	 * @throws IllegalArgumentException if the symbol is out of range
	 * @throws ArithmeticException if an arithmetic overflow occurs
	 */
	public void increment(int symbol);
	
	
	/**
	 * Returns the total of all symbol frequencies. The returned value is at
	 * least 0 and is always equal to {@code getHigh(getSymbolLimit() - 1)}.
	 * @return the total of all symbol frequencies
	 */
	public int getTotal();
	
	
	/**
	 * Returns the sum of the frequencies of all the symbols strictly
	 * below the specified symbol value. The returned value is at least 0.
	 * @param symbol the symbol to query
	 * @return the sum of the frequencies of all the symbols below {@code symbol}
	 * @throws IllegalArgumentException if the symbol is out of range
	 */
	public int getLow(int symbol);
	
	
	/**
	 * Returns the sum of the frequencies of the specified symbol
	 * and all the symbols below. The returned value is at least 0.
	 * @param symbol the symbol to query
	 * @return the sum of the frequencies of {@code symbol} and all symbols below
	 * @throws IllegalArgumentException if the symbol is out of range
	 */
	public int getHigh(int symbol);
	
}
