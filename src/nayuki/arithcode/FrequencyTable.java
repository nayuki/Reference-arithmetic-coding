package nayuki.arithcode;


/**
 * A table of symbol frequencies. Mutable.
 */
public final class FrequencyTable {
	
	private int[] frequencies;
	
	private int[] cumulative;  // Initialized lazily
	
	
	
	public FrequencyTable(int[] freqs) {
		if (freqs == null)
			throw new NullPointerException("Argument is null");
		if (freqs.length < 2)
			throw new IllegalArgumentException("At least 2 symbols needed");
		
		frequencies = freqs.clone();  // Defensive copy
		for (int x : frequencies) {
			if (x < 0)
				throw new IllegalArgumentException("Negative frequency");
		}
		cumulative = null;
	}
	
	
	
	public int getSymbolLimit() {
		return frequencies.length;
	}
	
	
	public int get(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		return frequencies[symbol];
	}
	
	
	public void set(int symbol, int freq) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		frequencies[symbol] = freq;
		cumulative = null;
	}
	
	
	public void increment(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		if (frequencies[symbol] == Integer.MAX_VALUE)
			throw new RuntimeException("Arithmetic overflow");
		frequencies[symbol]++;
		cumulative = null;
	}
	
	
	public int getTotal() {
		if (cumulative == null)
			initCumulative();
		return cumulative[frequencies.length];
	}
	
	
	public int getLow(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		if (cumulative == null)
			initCumulative();
		return cumulative[symbol];
	}
	
	
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
			cumulative[i] = sum;
			long temp = sum + frequencies[i];
			if (temp > Integer.MAX_VALUE)
				throw new RuntimeException("Arithmetic overflow");
			sum = (int)temp;
		}
		cumulative[frequencies.length] = sum;
	}
	
	
	// Returns a string showing all the symbols and frequencies. The format is subject to change. Useful for debugging.
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < frequencies.length; i++)
			sb.append(String.format("%d\t%d%n", i, frequencies[i]));
		return sb.toString();
	}
	
}
