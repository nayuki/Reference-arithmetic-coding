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
