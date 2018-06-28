/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#include <limits>
#include "ArithmeticCoder.hpp"

using std::uint32_t;
using std::uint64_t;


ArithmeticCoderBase::ArithmeticCoderBase() {
	STATE_SIZE = 32;
	MAX_RANGE = UINT64_C(1) << STATE_SIZE;
	MIN_RANGE = (MAX_RANGE >> 2) + 2;
	MAX_TOTAL = std::min(std::numeric_limits<decltype(MAX_RANGE)>::max() / MAX_RANGE, MIN_RANGE);
	MASK = MAX_RANGE - 1;
	TOP_MASK = MAX_RANGE >> 1;
	SECOND_MASK = TOP_MASK >> 1;
	low = 0;
	high = MASK;
}


void ArithmeticCoderBase::update(const FrequencyTable &freqs, uint32_t symbol) {
	// State check
	if (low >= high || (low & MASK) != low || (high & MASK) != high)
		throw "Assertion error: Low or high out of range";
	uint64_t range = high - low + 1;
	if (range < MIN_RANGE || range > MAX_RANGE)
		throw "Assertion error: Range out of range";
	
	// Frequency table values check
	uint32_t total = freqs.getTotal();
	uint32_t symLow = freqs.getLow(symbol);
	uint32_t symHigh = freqs.getHigh(symbol);
	if (symLow == symHigh)
		throw "Symbol has zero frequency";
	if (total > MAX_TOTAL)
		throw "Cannot code symbol because total is too large";
	
	// Update range
	uint64_t newLow  = low + symLow  * range / total;
	uint64_t newHigh = low + symHigh * range / total - 1;
	low = newLow;
	high = newHigh;
	
	// While the highest bits are equal
	while (((low ^ high) & TOP_MASK) == 0) {
		shift();
		low = (low << 1) & MASK;
		high = ((high << 1) & MASK) | 1;
	}
	
	// While the second highest bit of low is 1 and the second highest bit of high is 0
	while ((low & ~high & SECOND_MASK) != 0) {
		underflow();
		low = (low << 1) & (MASK >> 1);
		high = ((high << 1) & (MASK >> 1)) | TOP_MASK | 1;
	}
}


ArithmeticDecoder::ArithmeticDecoder(BitInputStream &in) :
		input(in),
		code(0) {
	for (int i = 0; i < STATE_SIZE; i++)
		code = code << 1 | readCodeBit();
}


uint32_t ArithmeticDecoder::read(const FrequencyTable &freqs) {
	// Translate from coding range scale to frequency table scale
	uint32_t total = freqs.getTotal();
	if (total > MAX_TOTAL)
		throw "Cannot decode symbol because total is too large";
	uint64_t range = high - low + 1;
	uint64_t offset = code - low;
	uint64_t value = ((offset + 1) * total - 1) / range;
	if (value * range / total > offset)
		throw "Assertion error";
	if (value >= total)
		throw "Assertion error";
	
	// A kind of binary search. Find highest symbol such that freqs.getLow(symbol) <= value.
	uint32_t start = 0;
	uint32_t end = freqs.getSymbolLimit();
	while (end - start > 1) {
		uint32_t middle = (start + end) >> 1;
		if (freqs.getLow(middle) > value)
			end = middle;
		else
			start = middle;
	}
	if (start + 1 != end)
		throw "Assertion error";
	
	uint32_t symbol = start;
	if (offset < freqs.getLow(symbol) * range / total || freqs.getHigh(symbol) * range / total <= offset)
		throw "Assertion error";
	update(freqs, symbol);
	if (code < low || code > high)
		throw "Assertion error: Code out of range";
	return symbol;
}


void ArithmeticDecoder::shift() {
	code = ((code << 1) & MASK) | readCodeBit();
}


void ArithmeticDecoder::underflow() {
	code = (code & TOP_MASK) | ((code << 1) & (MASK >> 1)) | readCodeBit();
}


int ArithmeticDecoder::readCodeBit() {
	int temp = input.read();
	if (temp == -1)
		temp = 0;
	return temp;
}


ArithmeticEncoder::ArithmeticEncoder(BitOutputStream &out) :
	output(out),
	numUnderflow(0) {}


void ArithmeticEncoder::write(const FrequencyTable &freqs, uint32_t symbol) {
	update(freqs, symbol);
}


void ArithmeticEncoder::finish() {
	output.write(1);
}


void ArithmeticEncoder::shift() {
	int bit = static_cast<int>(low >> (STATE_SIZE - 1));
	output.write(bit);
	
	// Write out the saved underflow bits
	for (; numUnderflow > 0; numUnderflow--)
		output.write(bit ^ 1);
}


void ArithmeticEncoder::underflow() {
	if (numUnderflow == std::numeric_limits<decltype(numUnderflow)>::max())
		throw "Maximum underflow reached";
	numUnderflow++;
}
