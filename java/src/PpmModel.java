/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */


import java.util.Arrays;

final class PpmModel {
	
	/*---- Fields ----*/
	
	public final int modelOrder;
	
	private final int symbolLimit;
	final int escapeSymbol;
	
	public final Context rootContext;
	public final FrequencyTable orderMinus1Freqs;
	
	
	
	/*---- Constructors ----*/
	
	public PpmModel(int order, int numSymbols) {
		if (order < -1 || numSymbols <= 1)
			throw new IllegalArgumentException();
		this.modelOrder = order;
		this.symbolLimit = numSymbols + 1;
		this.escapeSymbol = numSymbols;
		
		if (order >= 0) {
			rootContext = new Context(symbolLimit);
		} else
			rootContext = null;
		orderMinus1Freqs = new FlatFrequencyTable(symbolLimit);
	}
	
	
	
	/*---- Methods ----*/
	
	public void incrementContexts(int[] history, int symbol) {
		if (modelOrder == -1)
			return;
		if (history.length > modelOrder || symbol < 0 || symbol >= symbolLimit)
			throw new IllegalArgumentException();
		
		Context ctx = rootContext;
		ctx.frequencies.increment(symbol);
		for (int sym : history) {
			Context[] subctxs = ctx.getSubcontexts();
			
			if (subctxs[sym] == null) {
				subctxs[sym] = new Context(symbolLimit);
			}
			ctx = subctxs[sym];
			ctx.frequencies.increment(symbol);
		}
	}
	
	public int[] addToHistory(int[] history, int symbol) {
		if (modelOrder >= 1) {
			// Prepend current symbol, dropping oldest symbol if necessary
			if (history.length < modelOrder)
				history = Arrays.copyOf(history, history.length + 1);
			System.arraycopy(history, 0, history, 1, history.length - 1);
			history[0] = symbol;
		}
		return history;
	}
	
	
	/*---- Helper structure ----*/
	
	public static final class Context {
		
		public final FrequencyTable frequencies;
		
		private Context[] subcontexts;
		
		public Context(int symbols) {
			frequencies = new SimpleFrequencyTable(new int[symbols]);
			frequencies.increment(symbols - 1);
		}
		
		public Context[] getSubcontexts() {
			if (subcontexts == null) {
				subcontexts = new Context[frequencies.getSymbolLimit()];
			}
			return subcontexts;
		}
	}
	
}
