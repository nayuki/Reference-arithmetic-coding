# 
# Compression application using prediction by partial matching (PPM) with arithmetic coding
# 
# Usage: python ppm-compress.py InputFile OutputFile
# Then use the corresponding ppm-decompress.py application to recreate the original input file.
# Note that both the compressor and decompressor need to use the same PPM context modeling logic.
# The PPM algorithm can be thought of as a powerful generalization of adaptive arithmetic coding.
# 
# Copyright (c) Project Nayuki
# 
# https://www.nayuki.io/page/reference-arithmetic-coding
# https://github.com/nayuki/Reference-arithmetic-coding
# 

import contextlib, sys
import arithmeticcoding, ppmmodel
python3 = sys.version_info.major >= 3


# Must be at least -1 and match ppm-decompress.py. Warning: Exponential memory usage at O(257^n).
MODEL_ORDER = 3


# Command line main application function.
def main(args):
	# Handle command line arguments
	if len(args) != 2:
		sys.exit("Usage: python ppm-compress.py InputFile OutputFile")
	inputfile  = args[0]
	outputfile = args[1]
	
	# Perform file compression
	with open(inputfile, "rb") as inp, \
			contextlib.closing(arithmeticcoding.BitOutputStream(open(outputfile, "wb"))) as bitout:
		compress(inp, bitout)


def compress(inp, bitout):
	# Set up encoder and model. In this PPM model, symbol 256 represents EOF;
	# its frequency is 1 in the order -1 context but its frequency
	# is 0 in all other contexts (which have non-negative order).
	enc = arithmeticcoding.ArithmeticEncoder(32, bitout)
	model = ppmmodel.PpmModel(MODEL_ORDER, 257, 256)
	history = []
	
	while True:
		# Read and encode one byte
		symbol = inp.read(1)
		if len(symbol) == 0:
			break
		symbol = symbol[0] if python3 else ord(symbol)
		encode_symbol(model, history, symbol, enc)
		model.increment_contexts(history, symbol)
		
		if model.model_order >= 1:
			# Append current symbol or shift back by one
			if len(history) == model.model_order:
				del history[0]
			history.append(symbol)
	
	encode_symbol(model, history, 256, enc)  # EOF
	enc.finish()  # Flush remaining code bits


def encode_symbol(model, history, symbol, enc):
	# Try to use highest order context that exists based on the history suffix, such
	# that the next symbol has non-zero frequency. When symbol 256 is produced at a context
	# at any non-negative order, it means "escape to the next lower order with non-empty
	# context". When symbol 256 is produced at the order -1 context, it means "EOF".
	for order in reversed(range(len(history) + 1)):
		ctx = model.root_context
		# Note: We can't simplify the slice start to just '-order' because order can be 0
		for sym in history[len(history) - order : ]:
			assert ctx.subcontexts is not None
			ctx = ctx.subcontexts[sym]
			if ctx is None:
				break
		else:  # ctx is not None
			if symbol != 256 and ctx.frequencies.get(symbol) > 0:
				enc.write(ctx.frequencies, symbol)
				return
			# Else write context escape symbol and continue decrementing the order
			enc.write(ctx.frequencies, 256)
	# Logic for order = -1
	enc.write(model.order_minus1_freqs, symbol)


# Main launcher
if __name__ == "__main__":
	main(sys.argv[1 : ])
