package nayuki.arithcode;

import java.io.IOException;


public final class ArithmeticEncoder extends ArithmeticCoderBase {
	
	private BitOutputStream output;
	
	private int underflow;
	
	
	
	public ArithmeticEncoder(BitOutputStream out) {
		super();
		if (out == null)
			throw new NullPointerException();
		output = out;
		underflow = 0;
	}
	
	
	
	public void write(FrequencyTable freq, int symbol) throws IOException {
		update(freq, symbol);
	}
	
	
	protected void overflow() throws IOException {
		if (underflow == Integer.MAX_VALUE)
			throw new RuntimeException("Maximum underflow reached");
		
		int bit = (int)(low >>> (STATE_SIZE - 1));
		output.write(bit);
		
		// Write out saved underflow bits
		for (; underflow > 0; underflow--)
			output.write(bit ^ 1);
	}
	
	
	protected void underflow() throws IOException {
		underflow++;
	}
	
	
	public void finish() throws IOException {
		output.write(1);
	}
	
}
