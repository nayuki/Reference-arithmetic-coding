/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#include "FrequencyTable.hpp"

using std::uint32_t;


SimpleFrequencyTable::SimpleFrequencyTable(const std::vector<uint32_t> &freqs) {
	if (freqs.size() < 1)
		throw "At least 1 symbol needed";
	if (freqs.size() > UINT32_MAX - 1)
		throw "Too many symbols";
	frequencies = freqs;
	total = 0;
	for (uint32_t x : frequencies)
		total = checkedAdd(x, total);
	cumulative.reserve(frequencies.size() + 1);
}


uint32_t SimpleFrequencyTable::getSymbolLimit() const {
	return static_cast<uint32_t>(frequencies.size());
}


uint32_t SimpleFrequencyTable::get(uint32_t symbol) const {
	return frequencies.at(symbol);
}


void SimpleFrequencyTable::set(uint32_t symbol, uint32_t freq) {
	if (total < frequencies.at(symbol))
		throw "Assertion error";
	uint32_t temp = total - frequencies.at(symbol);
	total = checkedAdd(temp, freq);
	frequencies.at(symbol) = freq;
	cumulative.clear();
}


void SimpleFrequencyTable::increment(uint32_t symbol) {
	if (frequencies.at(symbol) == UINT32_MAX)
		throw "Arithmetic overflow";
	total = checkedAdd(total, 1);
	frequencies.at(symbol)++;
	cumulative.clear();
}


uint32_t SimpleFrequencyTable::getTotal() const {
	return total;
}


uint32_t SimpleFrequencyTable::getLow(uint32_t symbol) const {
	if (cumulative.empty())
		initCumulative();
	return cumulative.at(symbol);
}


uint32_t SimpleFrequencyTable::getHigh(uint32_t symbol) const {
	if (cumulative.empty())
		initCumulative();
	return cumulative.at(symbol + 1);
}


void SimpleFrequencyTable::initCumulative() const {
	uint32_t sum = 0;
	cumulative.push_back(sum);
	for (uint32_t freq : frequencies) {
		// This arithmetic should not throw an exception, because invariants are being maintained
		// elsewhere in the data structure. This implementation is just a defensive measure.
		sum = checkedAdd(freq, sum);
		cumulative.push_back(sum);
	}
	if (sum != total)
		throw "Assertion error";
}


uint32_t SimpleFrequencyTable::checkedAdd(uint32_t x, uint32_t y) {
	if (x > UINT32_MAX - y)
		throw "Arithmetic overflow";
	return x + y;
}
