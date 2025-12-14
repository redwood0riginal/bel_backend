package com.sypexfs.msin_bourse_enligne.market.service;

import com.sypexfs.msin_bourse_enligne.market.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MarketService {

    // Instrument operations
    List<MarketInstrument> getAllInstruments();
    Optional<MarketInstrument> getInstrumentBySymbol(String symbol);
    List<MarketInstrument> getInstrumentsBySector(String sector);
    List<MarketInstrument> getActiveInstruments();
    List<MarketInstrument> searchInstruments(String keyword);
    List<String> getAllSectors();
    MarketInstrument saveInstrument(MarketInstrument instrument);
    void deleteInstrument(Long id);

    // Market Summary operations
    List<MarketSummary> getAllSummaries();
    Optional<MarketSummary> getLatestSummaryBySymbol(String symbol);
    List<MarketSummary> getTopGainers(int limit);
    List<MarketSummary> getTopLosers(int limit);
    List<MarketSummary> getMostActive(int limit);
    List<MarketSummary> getSummariesByDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    MarketSummary saveSummary(MarketSummary summary);

    // Index operations
    List<MarketIndex> getAllIndices();
    Optional<MarketIndex> getIndexByCode(String code);
    Optional<MarketIndex> getLatestIndexByCode(String code);
    List<MarketIndex> getIndicesByType(String indexType);
    MarketIndex saveIndex(MarketIndex index);

    // Index Summary operations
    List<MarketIndexSummary> getAllIndexSummaries();
    Optional<MarketIndexSummary> getLatestIndexSummaryBySymbol(String symbol);
    List<MarketIndexSummary> getIndexSummariesByDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    MarketIndexSummary saveIndexSummary(MarketIndexSummary summary);

    // Orderbook operations
    List<MarketOrderbook> getOrderbookBySymbol(String symbol);
    List<MarketOrderbook> getActiveOrderbookBySymbol(String symbol);
    List<MarketOrderbook> getBuyOrdersBySymbol(String symbol);
    List<MarketOrderbook> getSellOrdersBySymbol(String symbol);
    Optional<MarketOrderbook> getOrderByMarketId(String orderMarketId);
    MarketOrderbook saveOrder(MarketOrderbook order);
    void deleteOrder(Long id);

    // Transaction operations
    List<MarketTransaction> getTransactionsBySymbol(String symbol, int limit);
    List<MarketTransaction> getRecentTransactions(int limit);
    List<MarketTransaction> getTransactionsByDateRange(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    Optional<MarketTransaction> getTransactionByExecId(String execId);
    MarketTransaction saveTransaction(MarketTransaction transaction);

    // News operations
    List<MarketNews> getRecentNews(int limit);
    List<MarketNews> getNewsByUrgency(String urgency, int limit);
    List<MarketNews> searchNews(String keyword, int limit);
    MarketNews saveNews(MarketNews news);
    void deleteNews(Long id);

    // Market Data operations
    List<MarketSummary> getMarketOverview();
    List<MarketIndexSummary> getIndexOverview();
}
