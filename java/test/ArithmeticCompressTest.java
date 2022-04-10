/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

/**
 * Tests {@link ArithmeticCompress} coupled with {@link ArithmeticDecompress}.
 */
public class ArithmeticCompressTest extends ArithmeticCodingTest {
	
	@Override
	protected ByteTransformer getCompressor() {
		return new ArithmeticCompress();
	}
	
	@Override
	protected ByteTransformer getDecompressor() {
		return new ArithmeticDecompress();
	}
	
}
