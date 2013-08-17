package nayuki.arithcode;

import java.io.IOException;


public final class ArithmeticEncoder extends ArithmeticCoderBase {
	
	private BitOutputStream output;
	
	// Number of saved underflow bits. This value can grow without bound, so a truly correct implementation would use a BigInteger.
	private int underflow;
	
	
	
	// Creates an arithmetic coding encoder.
	public ArithmeticEncoder(BitOutputStream out) {
		super();
		if (out == null)
			throw new NullPointerException();
		output = out;
		underflow = 0;
	}
	
	
	
	// Encodes a symbol.
	public void write(FrequencyTable freq, int symbol) throws IOException {
		write(new CheckedFrequencyTable(freq), symbol);
	}
	
	
	// Encodes a symbol.
	public void write(CheckedFrequencyTable freq, int symbol) throws IOException {
		update(freq, symbol);
	}
	
	
	// Must be called at the end of the stream of input symbols, otherwise the output data cannot be decoded properly.
	public void finish() throws IOException {
		output.write(1);
	}
	
	
	
	protected void shift() throws IOException {
		int bit = (int)(low >>> (STATE_SIZE - 1));
		output.write(bit);
		
		// Write out saved underflow bits
		for (; underflow > 0; underflow--)
			output.write(bit ^ 1);
	}
	
	
	protected void underflow() throws IOException {
		if (underflow == Integer.MAX_VALUE)
			throw new RuntimeException("Maximum underflow reached");
		underflow++;
	}
	
}
