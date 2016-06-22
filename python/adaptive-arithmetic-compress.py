# 
# Reference arithmetic coding
# Copyright (c) Project Nayuki
# 
# https://www.nayuki.io/page/reference-arithmetic-coding
# https://github.com/nayuki/Reference-arithmetic-coding
# 

import sys
import arithmeticcoding
python3 = sys.version_info.major >= 3


# Command line main application function.
def main(args):
	# Handle command line arguments
	if len(args) != 2:
		print("Usage: python adaptive-arithmetic-compress.py InputFile OutputFile")
		sys.exit(1)
	inputfile  = args[0]
	outputfile = args[1]
	
	# Perform file compression
	inp = open(inputfile, "rb")
	bitout = arithmeticcoding.BitOutputStream(open(outputfile, "wb"))
	try:
		compress(inp, bitout)
	finally:
		bitout.close()
		inp.close()


def compress(inp, bitout):
	initfreqs = arithmeticcoding.FlatFrequencyTable(257)
	freqs = arithmeticcoding.SimpleFrequencyTable(initfreqs)
	enc = arithmeticcoding.ArithmeticEncoder(bitout)
	while True:
		# Read and encode one byte
		symbol = inp.read(1)
		if len(symbol) == 0:
			break
		symbol = symbol[0] if python3 else ord(symbol)
		enc.write(freqs, symbol)
		freqs.increment(symbol)
	enc.write(freqs, 256)  # EOF
	enc.finish()  # Flush remaining code bits


# Main launcher
if __name__ == "__main__":
	main(sys.argv[1 : ])
