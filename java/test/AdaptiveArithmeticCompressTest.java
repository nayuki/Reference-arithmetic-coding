/* 
 * Reference arithmetic coding
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/reference-arithmetic-coding
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Tests {@link AdaptiveArithmeticCompress} coupled with {@link AdaptiveArithmeticDecompress}.
 */
public class AdaptiveArithmeticCompressTest extends ArithmeticCodingTest {
	
	protected byte[] compress(byte[] b) throws IOException {
		InputStream in = new ByteArrayInputStream(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (BitOutputStream bitOut = new BitOutputStream(out)) {
			AdaptiveArithmeticCompress.compress(in, bitOut);
		}
		return out.toByteArray();
	}
	
	
	protected byte[] decompress(byte[] b) throws IOException {
		InputStream in = new ByteArrayInputStream(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		AdaptiveArithmeticDecompress.decompress(new BitInputStream(in), out);
		return out.toByteArray();
	}
	
}
