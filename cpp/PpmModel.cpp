/* 
 * Reference arithmetic coding
 * Copyright (c) Project Nayuki
 * 
 * https://www.nayuki.io/page/reference-arithmetic-coding
 * https://github.com/nayuki/Reference-arithmetic-coding
 */

#include <algorithm>
#include <cstddef>
#include <stdexcept>
#include "PpmModel.hpp"

using std::uint32_t;
using std::vector;


PpmModel::Context::Context(uint32_t symbols, bool hasSubctx) :
		frequencies(vector<uint32_t>(symbols, 0)) {
	if (hasSubctx) {
		for (uint32_t i = 0; i < symbols; i++)
			subcontexts.push_back(std::unique_ptr<Context>(nullptr));
	}
}


PpmModel::PpmModel(int order, uint32_t symLimit, uint32_t escapeSym) :
		modelOrder(order),
		symbolLimit(symLimit),
		escapeSymbol(escapeSym),
		rootContext(std::unique_ptr<Context>(nullptr)),
		orderMinus1Freqs(FlatFrequencyTable(symbolLimit)) {
	if (order < -1 || escapeSym >= symLimit)
		throw std::domain_error("Illegal argument");
	if (order >= 0) {
		rootContext.reset(new Context(symbolLimit, order >= 1));
		rootContext->frequencies.increment(escapeSymbol);
	}
}


void PpmModel::incrementContexts(const vector<uint32_t> &history, uint32_t symbol) {
	if (modelOrder == -1)
		return;
	if (history.size() > static_cast<unsigned int>(modelOrder) || symbol >= symbolLimit)
		throw std::invalid_argument("Illegal argument");
	
	Context *ctx = rootContext.get();
	ctx->frequencies.increment(symbol);
	std::size_t i = 0;
	for (uint32_t sym : history) {
		vector<std::unique_ptr<Context> > &subctxs = ctx->subcontexts;
		if (subctxs.empty())
			throw std::logic_error("Assertion error");
		
		std::unique_ptr<Context> &subctx = subctxs.at(sym);
		if (subctx.get() == nullptr) {
			subctx.reset(new Context(symbolLimit, i + 1 < static_cast<unsigned int>(modelOrder)));
			subctx->frequencies.increment(escapeSymbol);
		}
		ctx = subctx.get();
		ctx->frequencies.increment(symbol);
		i++;
	}
}
