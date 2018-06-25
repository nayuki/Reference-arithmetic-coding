/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#include <limits>
#include "BitIoStream.hpp"


BitInputStream::BitInputStream(std::istream &in) :
	input(in),
	currentByte(0),
	numBitsRemaining(0) {}
	
	
int BitInputStream::read() {
	if (currentByte == -1)
		return -1;
	if (numBitsRemaining == 0) {
		currentByte = input.get();  // Note: istream.get() returns int, not char
		if (currentByte == EOF)
			return -1;
		if (currentByte < 0 || currentByte > 255)
			throw "Assertion error";
		numBitsRemaining = 8;
	}
	if (numBitsRemaining <= 0)
		throw "Assertion error";
	numBitsRemaining--;
	return (currentByte >> numBitsRemaining) & 1;
}


int BitInputStream::readNoEof() {
	int result = read();
	if (result != -1)
		return result;
	else
		throw "End of stream";
}


BitOutputStream::BitOutputStream(std::ostream &out) :
	output(out),
	currentByte(0),
	numBitsFilled(0) {}


void BitOutputStream::write(int b) {
	if (b != 0 && b != 1)
		throw "Argument must be 0 or 1";
	currentByte = (currentByte << 1) | b;
	numBitsFilled++;
	if (numBitsFilled == 8) {
		if (std::numeric_limits<char>::is_signed)
			currentByte -= (currentByte >> 7) << 8;
		output.put(static_cast<char>(currentByte));
		currentByte = 0;
		numBitsFilled = 0;
	}
}


void BitOutputStream::finish() {
	while (numBitsFilled != 0)
		write(0);
}
