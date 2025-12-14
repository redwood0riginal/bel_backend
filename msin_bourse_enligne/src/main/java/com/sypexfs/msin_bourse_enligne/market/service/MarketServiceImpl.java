package com.sypexfs.msin_bourse_enligne.market.service;

import com.sypexfs.msin_bourse_enligne.market.entity.*;
import com.sypexfs.msin_bourse_enligne.market.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MarketServiceImpl implements MarketService {

    private final MarketInstrumentRepository instrumentRepository;
    private final MarketSummaryRepository summaryRepository;
    private final MarketIndexRepository indexRepository;
    private final MarketIndexSummaryRepository indexSummaryRepository;
    private final MarketOrderbookRepository orderbookRepository;
    private final MarketTransactionRepository transactionRepository;
    private final MarketNewsRepository newsRepository;

    // ==================== Instrument Operations ====================

    @Override
    public List<MarketInstrument> getAllInstruments() {
        log.debug("Fetching all market instruments");
        return instrumentRepository.findAll();
    }

    @Override
    public Optional<MarketInstrument> getInstrumentBySymbol(String symbol) {
        log.debug("Fetching instrument by symbol: {}", symbol);
        return instrumentRepository.findBySymbol(symbol);
    }

    @Override
    public List<MarketInstrument> getInstrumentsBySector(String sector) {
        log.debug("Fetching instruments by sector: {}", sector);
        return instrumentRepository.findBySector(sector);
    }

    @Override
    public List<MarketInstrument> getActiveInstruments() {
        log.debug("Fetching all active instruments");
        return instrumentRepository.findAllActiveInstruments();
    }

    @Override
    public List<MarketInstrument> searchInstruments(String keyword) {
        log.debug("Searching instruments with keyword: {}", keyword);
        return instrumentRepository.searchByKeyword(keyword);
    }

    @Override
    public List<String> getAllSectors() {
        log.debug("Fetching all distinct sectors");
        return instrumentRepository.findAllDistinctSectors();
    }

    @Override
    @Transactional
    public MarketInstrument saveInstrument(MarketInstrument instrument) {
        log.debug("Saving market instrument: {}", instrument.getSymbol());
        return instrumentRepository.save(instrument);
    }

    @Override
    @Transactional
    public void deleteInstrument(Long id) {
        log.debug("Deleting instrument with id: {}", id);
        instrumentRepository.deleteById(id);
    }

    // ==================== Market Summary Operations ====================

    @Override
    public List<MarketSummary> getAllSummaries() {
        log.debug("Fetching all market summaries");
        return summaryRepository.findAll();
    }

    @Override
    public Optional<MarketSummary> getLatestSummaryBySymbol(String symbol) {
        log.debug("Fetching latest summary for symbol: {}", symbol);
        return summaryRepository.findLatestBySymbol(symbol);
    }

    @Override
    public List<MarketSummary> getTopGainers(int limit) {
        log.debug("Fetching top {} gainers", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return summaryRepository.findTopGainers(pageable);
    }

    @Override
    public List<MarketSummary> getTopLosers(int limit) {
        log.debug("Fetching top {} losers", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return summaryRepository.findTopLosers(pageable);
    }

    @Override
    public List<MarketSummary> getMostActive(int limit) {
        log.debug("Fetching {} most active stocks", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return summaryRepository.findMostActive(pageable);
    }

    @Override
    public List<MarketSummary> getSummariesByDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching summaries for symbol: {} between {} and {}", symbol, startDate, endDate);
        return summaryRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }

    @Override
    @Transactional
    public MarketSummary saveSummary(MarketSummary summary) {
        log.debug("Saving market summary for symbol: {}", summary.getSymbol());
        return summaryRepository.save(summary);
    }

    // ==================== Index Operations ====================

    @Override
    public List<MarketIndex> getAllIndices() {
        log.debug("Fetching all market indices");
        return indexRepository.findAllOrderByDatePriceDesc();
    }

    @Override
    public Optional<MarketIndex> getIndexByCode(String code) {
        log.debug("Fetching index by code: {}", code);
        return indexRepository.findByCode(code);
    }

    @Override
    public Optional<MarketIndex> getLatestIndexByCode(String code) {
        log.debug("Fetching latest index for code: {}", code);
        return indexRepository.findLatestByCode(code);
    }

    @Override
    public List<MarketIndex> getIndicesByType(String indexType) {
        log.debug("Fetching indices by type: {}", indexType);
        return indexRepository.findByIndexType(indexType);
    }

    @Override
    @Transactional
    public MarketIndex saveIndex(MarketIndex index) {
        log.debug("Saving market index: {}", index.getCode());
        return indexRepository.save(index);
    }

    // ==================== Index Summary Operations ====================

    @Override
    public List<MarketIndexSummary> getAllIndexSummaries() {
        log.debug("Fetching all index summaries");
        return indexSummaryRepository.findAllOrderByDatePriceDesc();
    }

    @Override
    public Optional<MarketIndexSummary> getLatestIndexSummaryBySymbol(String symbol) {
        log.debug("Fetching latest index summary for symbol: {}", symbol);
        return indexSummaryRepository.findLatestBySymbol(symbol);
    }

    @Override
    public List<MarketIndexSummary> getIndexSummariesByDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching index summaries for symbol: {} between {} and {}", symbol, startDate, endDate);
        return indexSummaryRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }

    @Override
    @Transactional
    public MarketIndexSummary saveIndexSummary(MarketIndexSummary summary) {
        log.debug("Saving index summary for symbol: {}", summary.getSymbol());
        return indexSummaryRepository.save(summary);
    }

    // ==================== Orderbook Operations ====================

    @Override
    public List<MarketOrderbook> getOrderbookBySymbol(String symbol) {
        log.debug("Fetching orderbook for symbol: {}", symbol);
        return orderbookRepository.findBySymbol(symbol);
    }

    @Override
    public List<MarketOrderbook> getActiveOrderbookBySymbol(String symbol) {
        log.debug("Fetching active orderbook for symbol: {}", symbol);
        return orderbookRepository.findActiveOrderbookBySymbol(symbol);
    }

    @Override
    public List<MarketOrderbook> getBuyOrdersBySymbol(String symbol) {
        log.debug("Fetching buy orders for symbol: {}", symbol);
        return orderbookRepository.findActiveOrdersBySymbolAndSide(symbol, "BUY");
    }

    @Override
    public List<MarketOrderbook> getSellOrdersBySymbol(String symbol) {
        log.debug("Fetching sell orders for symbol: {}", symbol);
        return orderbookRepository.findActiveOrdersBySymbolAndSide(symbol, "SELL");
    }

    @Override
    public Optional<MarketOrderbook> getOrderByMarketId(String orderMarketId) {
        log.debug("Fetching order by market id: {}", orderMarketId);
        return orderbookRepository.findByOrderMarketId(orderMarketId);
    }

    @Override
    @Transactional
    public MarketOrderbook saveOrder(MarketOrderbook order) {
        log.debug("Saving orderbook entry for symbol: {}", order.getSymbol());
        return orderbookRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.debug("Deleting order with id: {}", id);
        orderbookRepository.deleteById(id);
    }

    // ==================== Transaction Operations ====================

    @Override
    public List<MarketTransaction> getTransactionsBySymbol(String symbol, int limit) {
        log.debug("Fetching {} transactions for symbol: {}", limit, symbol);
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findActiveTransactionsBySymbol(symbol, pageable);
    }

    @Override
    public List<MarketTransaction> getRecentTransactions(int limit) {
        log.debug("Fetching {} recent transactions", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findRecentTransactions(pageable);
    }

    @Override
    public List<MarketTransaction> getTransactionsByDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching transactions for symbol: {} between {} and {}", symbol, startDate, endDate);
        return transactionRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }

    @Override
    public Optional<MarketTransaction> getTransactionByExecId(String execId) {
        log.debug("Fetching transaction by exec id: {}", execId);
        return transactionRepository.findByExecId(execId);
    }

    @Override
    @Transactional
    public MarketTransaction saveTransaction(MarketTransaction transaction) {
        log.debug("Saving transaction for symbol: {}", transaction.getSymbol());
        return transactionRepository.save(transaction);
    }

    // ==================== News Operations ====================

    @Override
    public List<MarketNews> getRecentNews(int limit) {
        log.debug("Fetching {} recent news items", limit);
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findAllOrderByPublishedAtDesc(pageable);
    }

    @Override
    public List<MarketNews> getNewsByUrgency(String urgency, int limit) {
        log.debug("Fetching {} news items with urgency: {}", limit, urgency);
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findByUrgency(urgency, pageable);
    }

    @Override
    public List<MarketNews> searchNews(String keyword, int limit) {
        log.debug("Searching news with keyword: {}", keyword);
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.searchByHeadline(keyword, pageable);
    }

    @Override
    @Transactional
    public MarketNews saveNews(MarketNews news) {
        log.debug("Saving market news");
        return newsRepository.save(news);
    }

    @Override
    @Transactional
    public void deleteNews(Long id) {
        log.debug("Deleting news with id: {}", id);
        newsRepository.deleteById(id);
    }

    // ==================== Market Data Operations ====================

    @Override
    public List<MarketSummary> getMarketOverview() {
        log.debug("Fetching market overview");
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return summaryRepository.findRecentSummaries(yesterday);
    }

    @Override
    public List<MarketIndexSummary> getIndexOverview() {
        log.debug("Fetching index overview");
        return indexSummaryRepository.findAllOrderByDatePriceDesc();
    }
}
