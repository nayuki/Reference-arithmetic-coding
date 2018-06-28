# 
# Compression application using static arithmetic coding
# 
# Usage: python arithmetic-compress.py InputFile OutputFile
# Then use the corresponding arithmetic-decompress.py application to recreate the original input file.
# Note that the application uses an alphabet of 257 symbols - 256 symbols for the byte
# values and 1 symbol for the EOF marker. The compressed file format starts with a list
# of 256 symbol frequencies, and then followed by the arithmetic-coded data.
# 
# Copyright (c) Project Nayuki
# 
# https://www.nayuki.io/page/reference-arithmetic-coding
# https://github.com/nayuki/Reference-arithmetic-coding
# 

import contextlib, sys
import arithmeticcoding
python3 = sys.version_info.major >= 3


# Command line main application function.
def main(args):
	# Handle command line arguments
	if len(args) != 2:
		sys.exit("Usage: python arithmetic-compress.py InputFile OutputFile")
	inputfile, outputfile = args
	
	# Read input file once to compute symbol frequencies
	freqs = get_frequencies(inputfile)
	freqs.increment(256)  # EOF symbol gets a frequency of 1
	
	# Read input file again, compress with arithmetic coding, and write output file
	with open(inputfile, "rb") as inp, \
			contextlib.closing(arithmeticcoding.BitOutputStream(open(outputfile, "wb"))) as bitout:
		write_frequencies(bitout, freqs)
		compress(freqs, inp, bitout)


# Returns a frequency table based on the bytes in the given file.
# Also contains an extra entry for symbol 256, whose frequency is set to 0.
def get_frequencies(filepath):
	freqs = arithmeticcoding.SimpleFrequencyTable([0] * 257)
	with open(filepath, "rb") as input:
		while True:
			b = input.read(1)
			if len(b) == 0:
				break
			b = b[0] if python3 else ord(b)
			freqs.increment(b)
	return freqs


def write_frequencies(bitout, freqs):
	for i in range(256):
		write_int(bitout, 32, freqs.get(i))


def compress(freqs, inp, bitout):
	enc = arithmeticcoding.ArithmeticEncoder(32, bitout)
	while True:
		symbol = inp.read(1)
		if len(symbol) == 0:
			break
		symbol = symbol[0] if python3 else ord(symbol)
		enc.write(freqs, symbol)
	enc.write(freqs, 256)  # EOF
	enc.finish()  # Flush remaining code bits


# Writes an unsigned integer of the given bit width to the given stream.
def write_int(bitout, numbits, value):
	for i in reversed(range(numbits)):
		bitout.write((value >> i) & 1)  # Big endian


# Main launcher
if __name__ == "__main__":
	main(sys.argv[1 : ])
