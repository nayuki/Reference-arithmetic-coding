package nayuki.arithcode;


/**
 * A frequency table where every symbol has the same frequency of 1. Immutable.
 */
public final class FlatFrequencyTable implements FrequencyTable {
	
	private final int numSymbols;
	
	
	
	public FlatFrequencyTable(int numSyms) {
		if (numSyms <= 0)
			throw new IllegalArgumentException("Number of symbols must be positive");
		numSymbols = numSyms;
	}
	
	
	
	public int getSymbolLimit() {
		return numSymbols;
	}
	
	
	public int get(int symbol) {
		if (symbol < 0 || symbol >= numSymbols)
			throw new IllegalArgumentException("Symbol out of range");
		return 1;
	}
	
	
	public int getTotal() {
		return numSymbols;
	}
	
	
	public int getLow(int symbol) {
		if (symbol < 0 || symbol >= numSymbols)
			throw new IllegalArgumentException("Symbol out of range");
		return symbol;
	}
	
	
	public int getHigh(int symbol) {
		if (symbol < 0 || symbol >= numSymbols)
			throw new IllegalArgumentException("Symbol out of range");
		return symbol + 1;
	}
	
	
	public String toString() {
		return Integer.toString(numSymbols);
	}
	
	
	public void set(int symbol, int freq) {
		throw new UnsupportedOperationException();
	}
	
	
	public void increment(int symbol) {
		throw new UnsupportedOperationException();
	}
	
}
