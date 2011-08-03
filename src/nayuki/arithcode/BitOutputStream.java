package nayuki.arithcode;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A stream where bits can be written to.
 */
public final class BitOutputStream {
	
	private OutputStream output;  // Underlying byte stream to write to
	
	private int currentByte;  // Always in the range 0x00 to 0xFF
	
	private int numBitsInCurrentByte;  // Always between 0 and 7, inclusive
	
	
	
	public BitOutputStream(OutputStream out) {
		if (out == null)
			throw new NullPointerException("Argument is null");
		output = out;
		currentByte = 0;
		numBitsInCurrentByte = 0;
	}
	
	
	
	// Writes a bit to the stream. The specified bit must be 0 or 1.
	public void write(int b) throws IOException {
		if (!(b == 0 || b == 1))
			throw new IllegalArgumentException("Argument must be 0 or 1");
		currentByte = currentByte << 1 | b;
		numBitsInCurrentByte++;
		if (numBitsInCurrentByte == 8) {
			output.write(currentByte);
			numBitsInCurrentByte = 0;
		}
	}
	
	
	// Closes this stream and the underlying OutputStream. If called when this bit stream is not at a byte boundary, then the minimum number of zeros (between 0 and 7) are written as padding to reach a byte boundary.
	public void close() throws IOException {
		while (numBitsInCurrentByte != 0)
			write(0);
		output.close();
	}
	
}
