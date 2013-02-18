package nayuki.arithcode;


/**
 * A wrapper that checks the preconditions (arguments) and postconditions (return value) of all the frequency table methods.
 * Useful for finding faults in a frequency table implementation. Current this does not check arithmetic overflow conditions.
 */
public final class CheckedFrequencyTable implements FrequencyTable {
	
	// The underlying frequency table that holds the data.
	private FrequencyTable freqTable;
	
	
	
	public CheckedFrequencyTable(FrequencyTable freq) {
		if (freq == null)
			throw new NullPointerException();
		freqTable = freq;
	}
	
	
	
	public int getSymbolLimit() {
		int result = freqTable.getSymbolLimit();
		if (result <= 0)
			throw new IllegalStateException("Non-positive symbol limit");
		return result;
	}
	
	
	public int get(int symbol) {
		checkSymbolInRange(symbol);
		int result = freqTable.get(symbol);
		if (result < 0)
			throw new IllegalStateException("Negative symbol frequency");
		return result;
	}
	
	
	public int getTotal() {
		int result = freqTable.getTotal();
		if (result < 0)
			throw new IllegalStateException("Negative total frequency");
		return result;
	}
	
	
	public int getLow(int symbol) {
		checkSymbolInRange(symbol);
		int low   = freqTable.getLow (symbol);
		int high  = freqTable.getHigh(symbol);
		int total = freqTable.getTotal();
		if (!(0 <= low && low <= high && high <= total))
			throw new IllegalStateException("Symbol low cumulative frequency out of range");
		return low;
	}
	
	
	public int getHigh(int symbol) {
		checkSymbolInRange(symbol);
		int low   = freqTable.getLow (symbol);
		int high  = freqTable.getHigh(symbol);
		int total = freqTable.getTotal();
		if (!(0 <= low && low <= high && high <= total))
			throw new IllegalStateException("Symbol high cumulative frequency out of range");
		return high;
	}
	
	
	public String toString() {
		return "CheckFrequencyTable (" + freqTable.toString() + ")";
	}
	
	
	public void set(int symbol, int freq) {
		checkSymbolInRange(symbol);
		if (freq < 0)
			throw new IllegalArgumentException("Negative symbol frequency");
		freqTable.set(symbol, freq);
	}
	
	
	public void increment(int symbol) {
		checkSymbolInRange(symbol);
		freqTable.increment(symbol);
	}
	
	
	private void checkSymbolInRange(int symbol) {
		if (symbol < 0 || symbol >= getSymbolLimit())
			throw new IllegalArgumentException("Symbol out of range");
	}
	
}
