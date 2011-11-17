package nayuki.arithcode;


public final class CheckedFrequencyTable implements FrequencyTable {
	
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
		if (symbol < 0 || symbol >= getSymbolLimit())
			throw new IllegalArgumentException("Symbol out of range");
		
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
		if (symbol < 0 || symbol >= getSymbolLimit())
			throw new IllegalArgumentException("Symbol out of range");
		
		int low   = freqTable.getLow (symbol);
		int high  = freqTable.getHigh(symbol);
		int total = freqTable.getTotal();
		if (!(0 <= low && low <= high && high <= total))
			throw new IllegalStateException("Symbol low cumulative frequency out of range");
		return low;
	}
	
	
	public int getHigh(int symbol) {
		if (symbol < 0 || symbol >= getSymbolLimit())
			throw new IllegalArgumentException("Symbol out of range");
		
		int low   = freqTable.getLow (symbol);
		int high  = freqTable.getHigh(symbol);
		int total = freqTable.getTotal();
		if (!(0 <= low && low <= high && high <= total))
			throw new IllegalStateException("Symbol high cumulative frequency out of range");
		return high;
	}
	
	
	public String toString() {
		return freqTable.toString();
	}
	
	
	public void set(int symbol, int freq) {
		if (symbol < 0 || symbol >= getSymbolLimit())
			throw new IllegalArgumentException("Symbol out of range");
		if (freq < 0)
			throw new IllegalArgumentException("Negative symbol frequency");
		
		freqTable.set(symbol, freq);
	}
	
	
	public void increment(int symbol) {
		if (symbol < 0 || symbol >= getSymbolLimit())
			throw new IllegalArgumentException("Symbol out of range");
		
		freqTable.increment(symbol);
	}
	
}
