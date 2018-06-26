/* 
 * Compression application using prediction by partial matching (PPM) with arithmetic coding.
 * 
 * Usage: PpmCompress InputFile OutputFile
 * Then use the corresponding "PpmDecompress" application to recreate the original input file.
 * Note that both the compressor and decompressor need to use the same PPM context modeling logic.
 * The PPM algorithm can be thought of as a powerful generalization of adaptive arithmetic coding.
 * 
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#include <algorithm>
#include <cstddef>
#include <cstdint>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <vector>
#include "ArithmeticCoder.hpp"
#include "BitIoStream.hpp"
#include "PpmModel.hpp"

using std::uint32_t;
using std::vector;


// Must be at least -1 and match PpmDecompress. Warning: Exponential memory usage at O(257^n).
static constexpr int MODEL_ORDER = 3;


static void compress(std::ifstream &in, BitOutputStream &out);
static void encodeSymbol(PpmModel &model, const vector<uint32_t> &history, uint32_t symbol, ArithmeticEncoder &enc);


int main(int argc, char *argv[]) {
	// Handle command line arguments
	if (argc != 3) {
		std::cerr << "Usage: " << argv[0] << " InputFile OutputFile" << std::endl;
		return EXIT_FAILURE;
	}
	const char *inputFile  = argv[1];
	const char *outputFile = argv[2];
	
	// Perform file compression
	std::ifstream in(inputFile, std::ios::binary);
	std::ofstream out(outputFile, std::ios::binary);
	BitOutputStream bout(out);
	try {
		compress(in, bout);
		bout.finish();
		return EXIT_SUCCESS;
	} catch (const char *msg) {
		std::cerr << msg << std::endl;
		return EXIT_FAILURE;
	}
}


static void compress(std::ifstream &in, BitOutputStream &out) {
	// Set up encoder and model
	ArithmeticEncoder enc(out);
	PpmModel model(MODEL_ORDER, 257, 256);
	vector<uint32_t> history;
	
	while (true) {
		// Read and encode one byte
		int symbol = in.get();
		if (symbol == EOF)
			break;
		if (symbol < 0 || symbol > 255)
			throw "Assertion error";
		uint32_t sym = static_cast<uint32_t>(symbol);
		encodeSymbol(model, history, sym, enc);
		model.incrementContexts(history, sym);
		
		if (model.modelOrder >= 1) {
			// Append current symbol or shift back by one
			if (history.size() >= static_cast<unsigned int>(model.modelOrder))
				history.erase(history.begin());
			history.push_back(sym);
		}
	}
	
	encodeSymbol(model, history, 256, enc);  // EOF
	enc.finish();  // Flush remaining code bits
}


static void encodeSymbol(PpmModel &model, const vector<uint32_t> &history, uint32_t symbol, ArithmeticEncoder &enc) {
	for (int order = std::min(static_cast<int>(history.size()), model.modelOrder); order >= 0; order--) {
		PpmModel::Context *ctx = model.rootContext.get();
		for (std::size_t i = history.size() - static_cast<unsigned int>(order); i < history.size(); i++) {
			if (ctx->subcontexts.empty())
				throw "Assertion error";
			ctx = ctx->subcontexts.at(history.at(i)).get();
			if (ctx == nullptr)
				goto outerEnd;
		}
		if (symbol != 256 && ctx->frequencies.get(symbol) > 0) {
			enc.write(ctx->frequencies, symbol);
			return;
		}
		// Else write context escape symbol and continue decrementing the order
		enc.write(ctx->frequencies, 256);
		outerEnd:;
	}
	// Logic for order = -1
	enc.write(model.orderMinus1Freqs, symbol);
}
