/* 
 * Compression application using static arithmetic coding
 * 
 * Usage: ArithmeticCompress InputFile OutputFile
 * Then use the corresponding "ArithmeticDecompress" application to recreate the original input file.
 * Note that the application uses an alphabet of 257 symbols - 256 symbols for the byte
 * values and 1 symbol for the EOF marker. The compressed file format starts with a list
 * of 256 symbol frequencies, and then followed by the arithmetic-coded data.
 * 
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#include <cstdint>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <stdexcept>
#include <vector>
#include "ArithmeticCoder.hpp"
#include "BitIoStream.hpp"
#include "FrequencyTable.hpp"

using std::uint32_t;


int main(int argc, char *argv[]) {
	// Handle command line arguments
	if (argc != 3) {
		std::cerr << "Usage: " << argv[0] << " InputFile OutputFile" << std::endl;
		return EXIT_FAILURE;
	}
	const char *inputFile  = argv[1];
	const char *outputFile = argv[2];
	
	// Read input file once to compute symbol frequencies
	std::ifstream in(inputFile, std::ios::binary);
	SimpleFrequencyTable freqs(std::vector<uint32_t>(257, 0));
	freqs.increment(256);  // EOF symbol gets a frequency of 1
	while (true) {
		int b = in.get();
		if (b == EOF)
			break;
		if (b < 0 || b > 255)
			throw std::logic_error("Assertion error");
		freqs.increment(static_cast<uint32_t>(b));
	}
	
	// Read input file again, compress with arithmetic coding, and write output file
	in.clear();
	in.seekg(0);
	std::ofstream out(outputFile, std::ios::binary);
	BitOutputStream bout(out);
	try {
		
		// Write frequency table
		for (uint32_t i = 0; i < 256; i++) {
			uint32_t freq = freqs.get(i);
			for (int j = 31; j >= 0; j--)
				bout.write(static_cast<int>((freq >> j) & 1));  // Big endian
		}
		
		ArithmeticEncoder enc(32, bout);
		while (true) {
			// Read and encode one byte
			int symbol = in.get();
			if (symbol == EOF)
				break;
			if (symbol < 0 || symbol > 255)
				throw std::logic_error("Assertion error");
			enc.write(freqs, static_cast<uint32_t>(symbol));
		}
		
		enc.write(freqs, 256);  // EOF
		enc.finish();  // Flush remaining code bits
		bout.finish();
		return EXIT_SUCCESS;
		
	} catch (const char *msg) {
		std::cerr << msg << std::endl;
		return EXIT_FAILURE;
	}
}
