package nayuki.arithcode;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;


public abstract class ArithmeticCodingTest {
	
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
	
	
	private void test(byte[] b) {
		try {
			byte[] compressed = compress(b);
			byte[] decompressed = decompress(compressed);
			assertArrayEquals(b, decompressed);
		} catch (EOFException e) {
			fail("Unexpected EOF");
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	
	protected abstract byte[] compress(byte[] b) throws IOException;
	
	protected abstract byte[] decompress(byte[] b) throws IOException;
	
}
