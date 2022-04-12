/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

/**
 * Tests {@link AdaptiveArithmeticCompress} coupled with {@link AdaptiveArithmeticDecompress}.
 */
public class AdaptiveArithmeticCompressTest extends ArithmeticCodingTest {
	
	@Override
	protected ByteTransformer getCompressor() {
		return new AdaptiveArithmeticCompress();
	}
	
	@Override
	protected ByteTransformer getDecompressor() {
		return new AdaptiveArithmeticDecompress();
	}
	
}
