/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ArithmeticCompressTest extends ArithmeticCodingTest {
	
	protected byte[] compress(byte[] b) throws IOException {
		InputStream in = new ByteArrayInputStream(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BitOutputStream bitOut = new BitOutputStream(out);
		
		FrequencyTable freq = getFrequencies(b);
		ArithmeticCompress.writeFrequencies(bitOut, freq);
		ArithmeticCompress.compress(freq, in, bitOut);
		bitOut.close();
		return out.toByteArray();
	}
	
	
	protected byte[] decompress(byte[] b) throws IOException {
		InputStream in = new ByteArrayInputStream(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BitInputStream bitIn = new BitInputStream(in);
		
		FrequencyTable freq = ArithmeticDecompress.readFrequencies(bitIn);
		ArithmeticDecompress.decompress(freq, bitIn, out);
		return out.toByteArray();
	}
	
	
	private static FrequencyTable getFrequencies(byte[] b) {
		FrequencyTable freq = new SimpleFrequencyTable(new int[257]);
		for (byte x : b)
			freq.increment(x & 0xFF);
		freq.increment(256);  // EOF symbol gets a frequency of 1
		return freq;
	}
	
}
