package nayuki.arithcode;


/**
 * A table of symbol frequencies. Possibly mutable. Primarily used for getting cumulative symbol frequencies.
 * Each symbol has a frequency, which is a non-negative integer.
 * The total of all symbol frequencies must not exceed Integer.MAX_VALUE.
 */
public interface FrequencyTable {
	
	// Returns the number of symbols in this table.
	public int getSymbolLimit();
	
	
	// Returns the frequency of the symbol.
	public int get(int symbol);
	
	
	// Sets the frequency of the symbol.
	public void set(int symbol, int freq);
	
	
	// Increments the frequency of the symbol.
	public void increment(int symbol);
	
	
	// Returns the total of all symbol frequencies.
	public int getTotal();
	
	
	// Returns the total of the frequencies of all the symbols below the given one.
	public int getLow(int symbol);
	
	
	// Returns the total of the frequencies of this symbol and all the ones below.
	public int getHigh(int symbol);
	
}
