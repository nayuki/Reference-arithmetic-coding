package nayuki.arithcode;


/**
 * A table of symbol frequencies. Mutable.
 * The current algorithm for calculating cumulative frequencies takes linear time, but there exist better algorithms.
 */
public final class SimpleFrequencyTable implements FrequencyTable {
	
	private int[] frequencies;
	
	private int[] cumulative;  // Initialized lazily. When this is not null, the data is valid.
	
	private int total;
	
	
	
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
	
	
	
	// Returns the number of symbols in this table.
	public int getSymbolLimit() {
		return frequencies.length;
	}
	
	
	// Returns the frequency of the symbol.
	public int get(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		return frequencies[symbol];
	}
	
	
	// Sets the frequency of the symbol.
	public void set(int symbol, int freq) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		if (freq < 0)
			throw new IllegalArgumentException("Negative frequency");
		
		total = checkedAdd(total - frequencies[symbol], freq);
		frequencies[symbol] = freq;
		cumulative = null;
	}
	
	
	// Increments the frequency of the symbol.
	public void increment(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		if (frequencies[symbol] == Integer.MAX_VALUE)
			throw new RuntimeException("Arithmetic overflow");
		
		total = checkedAdd(total, 1);
		frequencies[symbol]++;
		cumulative = null;
	}
	
	
	// Returns the total of all symbol frequencies.
	public int getTotal() {
		return total;
	}
	
	
	// Returns the total of the frequencies of all the symbols below the given one.
	public int getLow(int symbol) {
		if (symbol < 0 || symbol >= frequencies.length)
			throw new IllegalArgumentException("Symbol out of range");
		
		if (cumulative == null)
			initCumulative();
		return cumulative[symbol];
	}
	
	
	// Returns the total of the frequencies of this symbol and all the ones below.
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
	
	
	// Returns a string showing all the symbols and frequencies. The format is subject to change. Useful for debugging.
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
