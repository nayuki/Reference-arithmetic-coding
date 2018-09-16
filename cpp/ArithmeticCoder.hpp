/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#pragma once

#include <algorithm>
#include <cstdint>
#include "BitIoStream.hpp"
#include "FrequencyTable.hpp"


/* 
 * Provides the state and behaviors that arithmetic coding encoders and decoders share.
 */
class ArithmeticCoderBase {
	
	/*---- Configuration fields ----*/
	
	// Number of bits for the 'low' and 'high' state variables. Must be in the range [1, 63].
	// - For state sizes less than the midpoint of around 32, larger values are generally better -
	//   they allow a larger maximum frequency total (maximumTotal), and they reduce the approximation
	//   error inherent in adapting fractions to integers; both effects reduce the data encoding loss
	//   and asymptotically approach the efficiency of arithmetic coding using exact fractions.
	// - But for state sizes greater than the midpoint, because intermediate computations are limited
	//   to the long integer type's 63-bit unsigned precision, larger state sizes will decrease the
	//   maximum frequency total, which might constrain the user-supplied probability model.
	// - Therefore numStateBits=32 is recommended as the most versatile setting
	//   because it maximizes maximumTotal (which ends up being slightly over 2^30).
	// - Note that numStateBits=63 is legal but useless because it implies maximumTotal=1,
	//   which means a frequency table can only support one symbol with non-zero frequency.
	protected: int numStateBits;
	
	// Maximum range (high+1-low) during coding (trivial), which is 2^numStateBits = 1000...000.
	protected: std::uint64_t fullRange;
	
	// The top bit at width numStateBits, which is 0100...000.
	protected: std::uint64_t halfRange;
	
	// The second highest bit at width numStateBits, which is 0010...000. This is zero when numStateBits=1.
	protected: std::uint64_t quarterRange;
	
	// Minimum range (high+1-low) during coding (non-trivial), which is 0010...010.
	protected: std::uint64_t minimumRange;
	
	// Maximum allowed total from a frequency table at all times during coding.
	protected: std::uint64_t maximumTotal;
	
	// Bit mask of numStateBits ones, which is 0111...111.
	protected: std::uint64_t stateMask;
	
	
	/*---- State fields ----*/
	
	// Low end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 0s.
	protected: std::uint64_t low;
	
	// High end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 1s.
	protected: std::uint64_t high;
	
	
	/*---- Constructor ----*/
	
	// Constructs an arithmetic coder, which initializes the code range.
	public: explicit ArithmeticCoderBase(int numBits);
	
	
	public: virtual ~ArithmeticCoderBase() = 0;
	
	
	/*---- Methods ----*/
	
	// Updates the code range (low and high) of this arithmetic coder as a result
	// of processing the given symbol with the given frequency table.
	// Invariants that are true before and after encoding/decoding each symbol
	// (letting fullRange = 2^numStateBits):
	// * 0 <= low <= code <= high < fullRange. ('code' exists only in the decoder.)
	//   Therefore these variables are unsigned integers of numStateBits bits.
	// * low < 1/2 * fullRange <= high.
	//   In other words, they are in different halves of the full range.
	// * (low < 1/4 * fullRange) || (high >= 3/4 * fullRange).
	//   In other words, they are not both in the middle two quarters.
	// * Let range = high - low + 1, then fullRange/4 < minimumRange <= range <= fullRange.
	//   These invariants for 'range' essentially dictate the maximum total that the incoming
	//   frequency table can have, such that intermediate calculations don't overflow.
	protected: virtual void update(const FrequencyTable &freqs, std::uint32_t symbol);
	
	
	// Called to handle the situation when the top bit of 'low' and 'high' are equal.
	protected: virtual void shift() = 0;
	
	
	// Called to handle the situation when low=01(...) and high=10(...).
	protected: virtual void underflow() = 0;
	
};



/* 
 * Reads from an arithmetic-coded bit stream and decodes symbols.
 */
class ArithmeticDecoder final : private ArithmeticCoderBase {
	
	/*---- Fields ----*/
	
	// The underlying bit input stream.
	private: BitInputStream &input;
	
	// The current raw code bits being buffered, which is always in the range [low, high].
	private: std::uint64_t code;
	
	
	/*---- Constructor ----*/
	
	// Constructs an arithmetic coding decoder based on the
	// given bit input stream, and fills the code bits.
	public: explicit ArithmeticDecoder(int numBits, BitInputStream &in);
	
	
	/*---- Methods ----*/
	
	// Decodes the next symbol based on the given frequency table and returns it.
	// Also updates this arithmetic coder's state and may read in some bits.
	public: std::uint32_t read(const FrequencyTable &freqs);
	
	
	protected: void shift() override;
	
	
	protected: void underflow() override;
	
	
	// Returns the next bit (0 or 1) from the input stream. The end
	// of stream is treated as an infinite number of trailing zeros.
	private: int readCodeBit();
	
};



/* 
 * Encodes symbols and writes to an arithmetic-coded bit stream.
 */
class ArithmeticEncoder final : private ArithmeticCoderBase {
	
	/*---- Fields ----*/
	
	// The underlying bit output stream.
	private: BitOutputStream &output;
	
	// Number of saved underflow bits. This value can grow without bound,
	// so a truly correct implementation would use a bigint.
	private: unsigned long numUnderflow;
	
	
	/*---- Constructor ----*/
	
	// Constructs an arithmetic coding encoder based on the given bit output stream.
	public: explicit ArithmeticEncoder(int numBits, BitOutputStream &out);
	
	
	/*---- Methods ----*/
	
	// Encodes the given symbol based on the given frequency table.
	// Also updates this arithmetic coder's state and may write out some bits.
	public: void write(const FrequencyTable &freqs, std::uint32_t symbol);
	
	
	// Terminates the arithmetic coding by flushing any buffered bits, so that the output can be decoded properly.
	// It is important that this method must be called at the end of the each encoding process.
	// Note that this method merely writes data to the underlying output stream but does not close it.
	public: void finish();
	
	
	protected: void shift() override;
	
	
	protected: void underflow() override;
	
};
