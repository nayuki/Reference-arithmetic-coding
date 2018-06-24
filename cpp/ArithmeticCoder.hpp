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
	
	/*---- Configuration ----*/
	
	// Number of bits for 'low' and 'high'. Configurable in the range [1, 62] (but possibly more restricted).
	protected: int STATE_SIZE = 32;
	
	
	/*---- Constants ----*/
	
	// Maximum range during coding (trivial), i.e. 1000...000.
	protected: std::uint64_t MAX_RANGE = UINT64_C(1) << STATE_SIZE;
	
	// Minimum range during coding (non-trivial), i.e. 010...010.
	protected: std::uint64_t MIN_RANGE = (MAX_RANGE >> 2) + 2;
	
	// Maximum allowed total frequency at all times during coding.
	protected: std::uint64_t MAX_TOTAL = std::min(UINT64_MAX / MAX_RANGE, MIN_RANGE);
	
	// Mask of STATE_SIZE ones, i.e. 111...111.
	protected: std::uint64_t MASK = MAX_RANGE - 1;
	
	// Mask of the top bit at width STATE_SIZE, i.e. 100...000.
	protected: std::uint64_t TOP_MASK = MAX_RANGE >> 1;
	
	// Mask of the second highest bit at width STATE_SIZE, i.e. 010...000.
	protected: std::uint64_t SECOND_MASK = TOP_MASK >> 1;
	
	
	/*---- Fields ----*/
	
	// Low end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 0s.
	protected: std::uint64_t low;
	
	// High end of this arithmetic coder's current range. Conceptually has an infinite number of trailing 1s.
	protected: std::uint64_t high;
	
	
	/*---- Constructor ----*/
	
	// Constructs an arithmetic coder, which initializes the code range.
	public: explicit ArithmeticCoderBase();
	
	
	/*---- Methods ----*/
	
	// Updates the code range (low and high) of this arithmetic coder as a result
	// of processing the given symbol with the given frequency table.
	// Invariants that are true before and after encoding/decoding each symbol:
	// * 0 <= low <= code <= high < 2^STATE_SIZE. ('code' exists only in the decoder.)
	//   Therefore these variables are unsigned integers of STATE_SIZE bits.
	// * (low < 1/2 * 2^STATE_SIZE) && (high >= 1/2 * 2^STATE_SIZE).
	//   In other words, they are in different halves of the full range.
	// * (low < 1/4 * 2^STATE_SIZE) || (high >= 3/4 * 2^STATE_SIZE).
	//   In other words, they are not both in the middle two quarters.
	// * Let range = high - low + 1, then MAX_RANGE/4 < MIN_RANGE <= range
	//   <= MAX_RANGE = 2^STATE_SIZE. These invariants for 'range' essentially dictate the maximum
	//   total that the incoming frequency table can have, such that intermediate calculations don't overflow.
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
	public: explicit ArithmeticDecoder(BitInputStream &in);
	
	
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
	// so a truly correct implementation would use a BigInteger.
	private: std::uint32_t numUnderflow;
	
	
	/*---- Constructor ----*/
	
	// Constructs an arithmetic coding encoder based on the given bit output stream.
	public: explicit ArithmeticEncoder(BitOutputStream &out);
	
	
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
