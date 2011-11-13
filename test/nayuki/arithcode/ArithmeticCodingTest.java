package nayuki.arithcode;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.junit.Test;


// Tests ArithmeticCompress coupled with ArithmeticDecompress.
public class ArithmeticCodingTest {
	
	@Test
	public void testEmpty() {
		test(new byte[0]);
	}
	
	
	@Test
	public void testOneSymbol() {
		test(new byte[10]);
	}
	
	
	@Test
	public void testSimple() {
		test(new byte[]{0, 3, 1, 2});
	}
	
	
	@Test
	public void testEveryByteValue() {
		byte[] b = new byte[256];
		for (int i = 0; i < b.length; i++)
			b[i] = (byte)i;
		test(b);
	}
	
	
	@Test
	public void testUnderflow() {
		test(new byte[]{0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2});
	}
	
	
	@Test
	public void testRandom() {
		for (int i = 0; i < 100; i++) {
			byte[] b = new byte[random.nextInt(1000)];
			random.nextBytes(b);
			test(b);
		}
	}
	
	
	
	private static Random random = new Random();
	
	
	private static void test(byte[] b) {
		try {
			FrequencyTable freq = makeCode(b);
			
			InputStream original = new ByteArrayInputStream(b);
			ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
			BitOutputStream compressedOutBit = new BitOutputStream(compressedOut);
			ArithmeticCompress.compress(freq, original, compressedOutBit);
			compressedOutBit.close();
			byte[] compressed = compressedOut.toByteArray();
			
			InputStream compressedIn = new ByteArrayInputStream(compressed);
			BitInputStream compressedInBit = new BitInputStream(compressedIn);
			ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
			ArithmeticDecompress.decompress(freq, compressedInBit, decompressed);
			
			assertArrayEquals(b, decompressed.toByteArray());
		} catch (EOFException e) {
			fail("Unexpected EOF");
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	
	// Makes the optimal static code for the given bytes
	private static FrequencyTable makeCode(byte[] b) {
		FrequencyTable freq = new SimpleFrequencyTable(new int[257]);
		for (byte x : b)
			freq.increment(x & 0xFF);
		freq.increment(256);  // EOF symbol
		return freq;
	}
	
}
