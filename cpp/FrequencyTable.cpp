/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#include <stdexcept>
#include "FrequencyTable.hpp"

using std::uint32_t;


FrequencyTable::~FrequencyTable() {}


FlatFrequencyTable::FlatFrequencyTable(uint32_t numSyms) :
		numSymbols(numSyms) {
	if (numSyms < 1)
		throw std::domain_error("Number of symbols must be positive");
}


uint32_t FlatFrequencyTable::getSymbolLimit() const {
	return numSymbols;
}


uint32_t FlatFrequencyTable::get(uint32_t symbol) const  {
	checkSymbol(symbol);
	return 1;
}


uint32_t FlatFrequencyTable::getTotal() const  {
	return numSymbols;
}


uint32_t FlatFrequencyTable::getLow(uint32_t symbol) const  {
	checkSymbol(symbol);
	return symbol;
}


uint32_t FlatFrequencyTable::getHigh(uint32_t symbol) const  {
	checkSymbol(symbol);
	return symbol + 1;
}


void FlatFrequencyTable::set(uint32_t, uint32_t)  {
	throw std::logic_error("Unsupported operation");
}


void FlatFrequencyTable::increment(uint32_t) {
	throw std::logic_error("Unsupported operation");
}


void FlatFrequencyTable::checkSymbol(uint32_t symbol) const {
	if (symbol >= numSymbols)
		throw std::domain_error("Symbol out of range");
}


SimpleFrequencyTable::SimpleFrequencyTable(const std::vector<uint32_t> &freqs) {
	if (freqs.size() > UINT32_MAX - 1)
		throw std::length_error("Too many symbols");
	uint32_t size = static_cast<uint32_t>(freqs.size());
	if (size < 1)
		throw std::invalid_argument("At least 1 symbol needed");
	
	frequencies = freqs;
	cumulative.reserve(size + 1);
	initCumulative(false);
	total = getHigh(size - 1);
}


SimpleFrequencyTable::SimpleFrequencyTable(const FrequencyTable &freqs) {
	uint32_t size = freqs.getSymbolLimit();
	if (size < 1)
		throw std::invalid_argument("At least 1 symbol needed");
	if (size > UINT32_MAX - 1)
		throw std::length_error("Too many symbols");
	
	frequencies.reserve(size + 1);
	for (uint32_t i = 0; i < size; i++)
		frequencies.push_back(freqs.get(i));
	
	cumulative.reserve(size + 1);
	initCumulative(false);
	total = getHigh(size - 1);
}


uint32_t SimpleFrequencyTable::getSymbolLimit() const {
	return static_cast<uint32_t>(frequencies.size());
}


uint32_t SimpleFrequencyTable::get(uint32_t symbol) const {
	return frequencies.at(symbol);
}


void SimpleFrequencyTable::set(uint32_t symbol, uint32_t freq) {
	if (total < frequencies.at(symbol))
		throw std::logic_error("Assertion error");
	uint32_t temp = total - frequencies.at(symbol);
	total = checkedAdd(temp, freq);
	frequencies.at(symbol) = freq;
	cumulative.clear();
}


void SimpleFrequencyTable::increment(uint32_t symbol) {
	if (frequencies.at(symbol) == UINT32_MAX)
		throw std::overflow_error("Arithmetic overflow");
	total = checkedAdd(total, 1);
	frequencies.at(symbol)++;
	cumulative.clear();
}


uint32_t SimpleFrequencyTable::getTotal() const {
	return total;
}


uint32_t SimpleFrequencyTable::getLow(uint32_t symbol) const {
	initCumulative();
	return cumulative.at(symbol);
}


uint32_t SimpleFrequencyTable::getHigh(uint32_t symbol) const {
	initCumulative();
	return cumulative.at(symbol + 1);
}


void SimpleFrequencyTable::initCumulative(bool checkTotal) const {
	if (!cumulative.empty())
		return;
	uint32_t sum = 0;
	cumulative.push_back(sum);
	for (uint32_t freq : frequencies) {
		// This arithmetic should not throw an exception, because invariants are being maintained
		// elsewhere in the data structure. This implementation is just a defensive measure.
		sum = checkedAdd(freq, sum);
		cumulative.push_back(sum);
	}
	if (checkTotal && sum != total)
		throw std::logic_error("Assertion error");
}


uint32_t SimpleFrequencyTable::checkedAdd(uint32_t x, uint32_t y) {
	if (x > UINT32_MAX - y)
		throw std::overflow_error("Arithmetic overflow");
	return x + y;
}
