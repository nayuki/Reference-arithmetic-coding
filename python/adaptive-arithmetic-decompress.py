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
		print("Usage: python adaptive-arithmetic-decompress.py InputFile OutputFile")
		sys.exit(1)
	inputfile  = args[0]
	outputfile = args[1]
	
	# Perform file decompression
	bitin = arithmeticcoding.BitInputStream(open(inputfile, "rb"))
	out = open(outputfile, "wb")
	try:
		decompress(bitin, out)
	finally:
		out.close()
		bitin.close()


def decompress(bitin, out):
	initfreqs = arithmeticcoding.FlatFrequencyTable(257)
	freqs = arithmeticcoding.SimpleFrequencyTable(initfreqs)
	dec = arithmeticcoding.ArithmeticDecoder(bitin)
	while True:
		# Decode and write one byte
		symbol = dec.read(freqs)
		if symbol == 256:  # EOF symbol
			break
		out.write(bytes((symbol,)) if python3 else chr(symbol))
		freqs.increment(symbol)


# Main launcher
if __name__ == "__main__":
	main(sys.argv[1 : ])
