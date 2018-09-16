/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */


final class PpmModel {
	
	/*---- Fields ----*/
	
	public final int modelOrder;
	
	private final int symbolLimit;
	private final int escapeSymbol;
	
	public final Context rootContext;
	public final FrequencyTable orderMinus1Freqs;
	
	
	
	/*---- Constructors ----*/
	
	public PpmModel(int order, int symbolLimit, int escapeSymbol) {
		if (order < -1 || symbolLimit <= 0 || escapeSymbol < 0 || escapeSymbol >= symbolLimit)
			throw new IllegalArgumentException();
		this.modelOrder = order;
		this.symbolLimit = symbolLimit;
		this.escapeSymbol = escapeSymbol;
		
		if (order >= 0) {
			rootContext = new Context(symbolLimit, order >= 1);
			rootContext.frequencies.increment(escapeSymbol);
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
		int i = 0;
		for (int sym : history) {
			Context[] subctxs = ctx.subcontexts;
			if (subctxs == null)
				throw new AssertionError();
			
			if (subctxs[sym] == null) {
				subctxs[sym] = new Context(symbolLimit, i + 1 < modelOrder);
				subctxs[sym].frequencies.increment(escapeSymbol);
			}
			ctx = subctxs[sym];
			ctx.frequencies.increment(symbol);
			i++;
		}
	}
	
	
	
	/*---- Helper structure ----*/
	
	public static final class Context {
		
		public final FrequencyTable frequencies;
		
		public final Context[] subcontexts;
		
		
		public Context(int symbols, boolean hasSubctx) {
			frequencies = new SimpleFrequencyTable(new int[symbols]);
			if (hasSubctx)
				subcontexts = new Context[symbols];
			else
				subcontexts = null;
		}
		
	}
	
}
