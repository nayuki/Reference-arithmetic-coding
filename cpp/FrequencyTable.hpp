/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#pragma once

#include <cstdint>
#include <vector>


/* 
 * A table of symbol frequencies. The table holds data for symbols numbered from 0
 * to getSymbolLimit()-1. Each symbol has a frequency, which is a non-negative integer.
 * Frequency table objects are primarily used for getting cumulative symbol
 * frequencies. These objects can be mutable depending on the implementation.
 * The total of all symbol frequencies must not exceed UINT32_MAX.
 */
class FrequencyTable {
	
	public: virtual ~FrequencyTable() = 0;
	
	
	// Returns the number of symbols in this frequency table, which is a positive number.
	public: virtual std::uint32_t getSymbolLimit() const = 0;
	
	
	// Returns the frequency of the given symbol.
	public: virtual std::uint32_t get(std::uint32_t symbol) const = 0;
	
	
	// Sets the frequency of the given symbol to the given value.
	public: virtual void set(std::uint32_t symbol, std::uint32_t freq) = 0;
	
	
	// Increments the frequency of the given symbol.
	public: virtual void increment(std::uint32_t symbol) = 0;
	
	
	// Returns the total of all symbol frequencies. The returned
	// value is always equal to getHigh(getSymbolLimit() - 1).
	public: virtual std::uint32_t getTotal() const = 0;
	
	
	// Returns the sum of the frequencies of all the symbols strictly below the given symbol value.
	public: virtual std::uint32_t getLow(std::uint32_t symbol) const = 0;
	
	
	// Returns the sum of the frequencies of the given symbol and all the symbols below.
	public: virtual std::uint32_t getHigh(std::uint32_t symbol) const = 0;
	
};



class FlatFrequencyTable final : public FrequencyTable {
	
	/*---- Fields ----*/
	
	// Total number of symbols, which is at least 1.
	private: std::uint32_t numSymbols;
	
	
	/*---- Constructor ----*/
	
	// Constructs a flat frequency table with the given number of symbols.
	public: explicit FlatFrequencyTable(std::uint32_t numSyms);
	
	
	/*---- Methods ----*/
	
	public: std::uint32_t getSymbolLimit() const override;
	
	
	public: std::uint32_t get(std::uint32_t symbol) const override;
	
	
	public: std::uint32_t getTotal() const override;
	
	
	public: std::uint32_t getLow(std::uint32_t symbol) const override;
	
	
	public: std::uint32_t getHigh(std::uint32_t symbol) const override;
	
	
	public: void set(std::uint32_t symbol, std::uint32_t freq) override;
	
	
	public: void increment(std::uint32_t symbol) override;
	
	
	private: void checkSymbol(std::uint32_t symbol) const;
	
};



/* 
 * A mutable table of symbol frequencies. The number of symbols cannot be changed
 * after construction. The current algorithm for calculating cumulative frequencies
 * takes linear time, but there exist faster algorithms such as Fenwick trees.
 */
class SimpleFrequencyTable final : public FrequencyTable {
	
	/*---- Fields ----*/
	
	// The frequency for each symbol. Its length is at least 1.
	private: std::vector<std::uint32_t> frequencies;
	
	// cumulative[i] is the sum of 'frequencies' from 0 (inclusive) to i (exclusive).
	// Initialized lazily. When its length is not zero, the data is valid.
	private: mutable std::vector<std::uint32_t> cumulative;
	
	// Always equal to the sum of 'frequencies'.
	private: std::uint32_t total;
	
	
	/*---- Constructors ----*/
	
	// Constructs a frequency table from the given array of symbol frequencies.
	// There must be at least 1 symbol, and the total must not exceed UINT32_MAX.
	public: explicit SimpleFrequencyTable(const std::vector<std::uint32_t> &freqs);
	
	
	// Constructs a frequency table by copying the given frequency table.
	public: explicit SimpleFrequencyTable(const FrequencyTable &freqs);
	
	
	/*---- Methods ----*/
	
	public: std::uint32_t getSymbolLimit() const override;
	
	
	public: std::uint32_t get(std::uint32_t symbol) const override;
	
	
	public: void set(std::uint32_t symbol, std::uint32_t freq) override;
	
	
	public: void increment(std::uint32_t symbol) override;
	
	
	public: std::uint32_t getTotal() const override;
	
	
	public: std::uint32_t getLow(std::uint32_t symbol) const override;
	
	
	public: std::uint32_t getHigh(std::uint32_t symbol) const override;
	
	
	// Recomputes the array of cumulative symbol frequencies.
	private: void initCumulative(bool checkTotal=true) const;
	
	
	// Adds the given integers, or throws an exception if the result cannot be represented as a uint32_t (i.e. overflow).
	private: static std::uint32_t checkedAdd(std::uint32_t x, std::uint32_t y);
	
};
