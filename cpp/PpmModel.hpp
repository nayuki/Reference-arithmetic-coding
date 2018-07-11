/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#pragma once

#include <cstdint>
#include <memory>
#include <vector>
#include "FrequencyTable.hpp"


class PpmModel final {
	
	/*---- Helper structure ----*/
	
	public: class Context final {
		
		public: SimpleFrequencyTable frequencies;
		
		public: std::vector<std::unique_ptr<Context> > subcontexts;
		
		
		public: explicit Context(std::uint32_t symbols, bool hasSubctx);
		
	};
	
	
	
	/*---- Fields ----*/
	
	public: int modelOrder;
	
	private: std::uint32_t symbolLimit;
	private: std::uint32_t escapeSymbol;
	
	public: std::unique_ptr<Context> rootContext;
	public: SimpleFrequencyTable orderMinus1Freqs;
	
	
	/*---- Constructor ----*/
	
	public: explicit PpmModel(int order, std::uint32_t symLimit, std::uint32_t escapeSym);
	
	
	/*---- Methods ----*/
	
	public: void incrementContexts(const std::vector<std::uint32_t> &history, std::uint32_t symbol);
	
	
	private: static std::vector<std::uint32_t> makeEmpty(std::uint32_t len);
	
};
