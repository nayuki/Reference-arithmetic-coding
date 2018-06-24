/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#pragma once

#include <istream>
#include <ostream>


/* 
 * A stream of bits that can be read. Because they come from an underlying byte stream,
 * the total number of bits is always a multiple of 8. The bits are read in big endian.
 */
class BitInputStream final {
	
	/*---- Fields ----*/
	
	// The underlying byte stream to read from.
	private: std::istream &input;
	
	// Either in the range [0x00, 0xFF] if bits are available, or -1 if end of stream is reached.
	private: int currentByte;
	
	// Number of remaining bits in the current byte, always between 0 and 7 (inclusive).
	private: int numBitsRemaining;
	
	
	/*---- Constructor ----*/
	
	// Constructs a bit input stream based on the given byte input stream.
	public: explicit BitInputStream(std::istream &in);
	
	
	/*---- Methods ----*/
	
	// Reads a bit from this stream. Returns 0 or 1 if a bit is available, or -1 if
	// the end of stream is reached. The end of stream always occurs on a byte boundary.
	public: int read();
	
	
	// Reads a bit from this stream. Returns 0 or 1 if a bit is available, or throws an exception
	// if the end of stream is reached. The end of stream always occurs on a byte boundary.
	public: int readNoEof();
	
};



/* 
 * A stream where bits can be written to. Because they are written to an underlying
 * byte stream, the end of the stream is padded with 0's up to a multiple of 8 bits.
 * The bits are written in big endian.
 */
class BitOutputStream final {
	
	/*---- Fields ----*/
	
	// The underlying byte stream to write to.
	private: std::ostream &output;
	
	// The accumulated bits for the current byte, always in the range [0x00, 0xFF].
	private: int currentByte;
	
	// Number of accumulated bits in the current byte, always between 0 and 7 (inclusive).
	private: int numBitsFilled;
	
	
	/*---- Constructor ----*/
	
	// Constructs a bit output stream based on the given byte output stream.
	public: explicit BitOutputStream(std::ostream &out);
	
	
	/*---- Methods ----*/
	
	// Writes a bit to the stream. The given bit must be 0 or 1.
	public: void write(int b);
	
	
	// Writes the minimum number of "0" bits (between 0 and 7 of them) as padding to
	// reach the next byte boundary. Most applications will require the bits in the last
	// partial byte to be written before the underlying stream is closed. Note that this
	// method merely writes data to the underlying output stream but does not close it.
	public: void finish();
	
};
