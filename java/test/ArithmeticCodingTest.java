/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.EOFException;
import java.io.IOException;
import java.util.Random;
import org.junit.Test;


/**
 * Tests the compression and decompression of a complete arithmetic coding application, using the JUnit test framework.
 */
public abstract class ArithmeticCodingTest {
	
	/*---- Test cases ----*/
	
	@Test public void testEmpty() {
		test(new byte[0]);
	}
	
	
	@Test public void testOneSymbol() {
		test(new byte[10]);
	}
	
	
	@Test public void testSimple() {
		test(new byte[]{0, 3, 1, 2});
	}
	
	
	@Test public void testEveryByteValue() {
		byte[] b = new byte[256];
		for (int i = 0; i < b.length; i++)
			b[i] = (byte)i;
		test(b);
	}
	
	
	@Test public void testUnderflow() {
		test(new byte[]{0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2});
	}
	
	
	@Test public void testUniformRandom() {
		for (int i = 0; i < 100; i++) {
			byte[] b = new byte[random.nextInt(1000)];
			random.nextBytes(b);
			test(b);
		}
	}
	
	
	@Test public void testRandomDistribution() {
		for (int i = 0; i < 1000; i++) {
			int m = random.nextInt(255) + 1;  // Number of different symbols present
			int n = Math.max(random.nextInt(1000), m);  // Length of message
			
			// Create distribution
			int[] freqs = new int[m];
			int sum = 0;
			for (int j = 0; j < freqs.length; j++) {
				freqs[j] = random.nextInt(10000) + 1;
				sum += freqs[j];
			}
			int total = sum;
			
			// Rescale frequencies
			sum = 0;
			int index = 0;
			for (int j = 0; j < freqs.length; j++) {
				int newsum = sum + freqs[j];
				int newindex = (n - m) * newsum / total + j + 1;
				freqs[j] = newindex - index;
				sum = newsum;
				index = newindex;
			}
			assertEquals(n, index);
			
			// Create symbols
			byte[] message = new byte[n];
			for (int k = 0, j = 0; k < freqs.length; k++) {
				for (int l = 0; l < freqs[k]; l++, j++)
					message[j] = (byte)k;
			}
			
			// Shuffle message (Durstenfeld algorithm)
			for (int j = 0; j < message.length; j++) {
				int k = random.nextInt(message.length - j) + j;
				byte temp = message[j];
				message[j] = message[k];
				message[k] = temp;
			}
			
			test(message);
		}
	}
	
	
	
	/*---- Utilities ----*/
	
	// Tests that the given byte array can be compressed and decompressed to the same data, and not throw any exceptions.
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
	
	
	private static Random random = new Random();
	
	
	
	/*---- Abstract methods ----*/
	
	// Compression method that needs to be supplied by a subclass.
	protected abstract byte[] compress(byte[] b) throws IOException;
	
	// Decompression method that needs to be supplied by a subclass.
	protected abstract byte[] decompress(byte[] b) throws IOException;
	
}
