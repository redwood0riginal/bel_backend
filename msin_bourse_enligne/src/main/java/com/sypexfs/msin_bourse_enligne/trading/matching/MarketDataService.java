package com.sypexfs.msin_bourse_enligne.trading.matching;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketSummary;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketTransaction;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketSummaryRepository;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing and updating market data
 * Integrates with matching engine to update prices and volumes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final MarketSummaryRepository summaryRepository;
    private final MarketTransactionRepository marketTransactionRepository;
    
    // Cache for current prices (symbol -> price)
    private final Map<String, BigDecimal> priceCache = new ConcurrentHashMap<>();
    
    // Cache for daily statistics (symbol -> DailyStats)
    private final Map<String, DailyStats> dailyStatsCache = new ConcurrentHashMap<>();

    /**
     * Get current price for a symbol
     */
    public BigDecimal getCurrentPrice(String symbol) {
        // Check cache first
        BigDecimal cachedPrice = priceCache.get(symbol);
        if (cachedPrice != null) {
            return cachedPrice;
        }
        
        // Fallback to database
        Optional<MarketSummary> summary = summaryRepository.findLatestBySymbol(symbol);
        if (summary.isPresent() && summary.get().getPrice() != null) {
            BigDecimal price = summary.get().getPrice();
            priceCache.put(symbol, price);
            return price;
        }
        
        log.warn("No price available for symbol: {}", symbol);
        return BigDecimal.ZERO;
    }

    /**
     * Update last trade information
     * Note: Does NOT create a market transaction - transactions are already created by TransactionSyncService
     */
    @Transactional
    public void updateLastTrade(String symbol, BigDecimal price, BigDecimal quantity, LocalDateTime timestamp) {
        log.debug("Updating last trade for {}: price={}, qty={}", symbol, price, quantity);
        
        // Update price cache
        priceCache.put(symbol, price);
        
        // Update daily statistics
        DailyStats stats = dailyStatsCache.computeIfAbsent(symbol, k -> new DailyStats());
        stats.updateWithTrade(price, quantity);
        
        // Update market summary (no need to create transaction - already done by TransactionSyncService)
        updateMarketSummary(symbol, price, quantity, timestamp);
    }

    /**
     * Update market summary with latest trade
     */
    private void updateMarketSummary(String symbol, BigDecimal price, BigDecimal quantity, LocalDateTime timestamp) {
        Optional<MarketSummary> existingSummary = summaryRepository.findLatestBySymbol(symbol);
        
        MarketSummary summary;
        if (existingSummary.isPresent()) {
            summary = existingSummary.get();
        } else {
            summary = new MarketSummary();
            summary.setSymbol(symbol);
            summary.setDateTrans(timestamp);
        }
        
        // Update price and volume
        summary.setPrice(price);
        summary.setDateTrans(timestamp);
        
        // Update volume
        BigDecimal currentVolume = summary.getVolume() != null ? summary.getVolume() : BigDecimal.ZERO;
        summary.setVolume(currentVolume.add(quantity));
        
        // Update daily high/low
        DailyStats stats = dailyStatsCache.get(symbol);
        if (stats != null) {
            summary.setHigherPrice(stats.getHigh());
            summary.setLowerPrice(stats.getLow());
            summary.setOpeningPrice(stats.getOpen());
            
            // Calculate variation
            if (stats.getOpen() != null && stats.getOpen().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal variation = price.subtract(stats.getOpen())
                    .divide(stats.getOpen(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                summary.setVariation(variation);
            }
        }
        
        summaryRepository.save(summary);
        log.debug("Updated market summary for {}: price={}", symbol, price);
    }

    /**
     * Get daily statistics for a symbol
     */
    public DailyStats getDailyStats(String symbol) {
        return dailyStatsCache.computeIfAbsent(symbol, k -> {
            DailyStats stats = new DailyStats();
            
            // Load from database
            Optional<MarketSummary> summary = summaryRepository.findLatestBySymbol(symbol);
            if (summary.isPresent()) {
                MarketSummary s = summary.get();
                stats.setOpen(s.getOpeningPrice());
                stats.setHigh(s.getHigherPrice());
                stats.setLow(s.getLowerPrice());
                stats.setClose(s.getPrice());
                stats.setVolume(s.getVolume());
            }
            
            return stats;
        });
    }

    /**
     * Initialize market data from database on startup
     */
    public void initializeMarketData() {
        log.info("Initializing market data cache");
        
        summaryRepository.findAll().forEach(summary -> {
            if (summary.getPrice() != null) {
                priceCache.put(summary.getSymbol(), summary.getPrice());
                
                DailyStats stats = new DailyStats();
                stats.setOpen(summary.getOpeningPrice());
                stats.setHigh(summary.getHigherPrice());
                stats.setLow(summary.getLowerPrice());
                stats.setClose(summary.getPrice());
                stats.setVolume(summary.getVolume());
                dailyStatsCache.put(summary.getSymbol(), stats);
            }
        });
        
        log.info("Initialized market data for {} symbols", priceCache.size());
    }

    /**
     * Reset daily statistics (called at market open)
     */
    public void resetDailyStats() {
        log.info("Resetting daily statistics");
        
        dailyStatsCache.forEach((symbol, stats) -> {
            BigDecimal currentPrice = priceCache.get(symbol);
            if (currentPrice != null) {
                stats.reset(currentPrice);
            }
        });
    }

    /**
     * Get market status for a symbol
     */
    public MarketStatus getMarketStatus(String symbol) {
        BigDecimal currentPrice = getCurrentPrice(symbol);
        Optional<MarketSummary> summary = summaryRepository.findLatestBySymbol(symbol);
        MarketSummary s = summary.orElse(null);
        
        DailyStats stats = getDailyStats(symbol);
        
        return new MarketStatus(
            symbol,
            currentPrice,
            s != null ? s.getOpeningPrice() : null,
            s != null ? s.getHigherPrice() : null,
            s != null ? s.getLowerPrice() : null,
            stats.getClose(),
            stats.getVolume(),
            stats.getTradeCount(),
            LocalDateTime.now()
        );
    }

    /**
     * Clear all caches
     */
    public void clearCaches() {
        priceCache.clear();
        dailyStatsCache.clear();
        log.info("Cleared market data caches");
    }

    /**
     * Daily statistics for a symbol
     */
    @lombok.Data
    public static class DailyStats {
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private BigDecimal volume = BigDecimal.ZERO;
        private long tradeCount = 0;
        
        public void updateWithTrade(BigDecimal price, BigDecimal quantity) {
            if (open == null) {
                open = price;
            }
            
            if (high == null || price.compareTo(high) > 0) {
                high = price;
            }
            
            if (low == null || price.compareTo(low) < 0) {
                low = price;
            }
            
            close = price;
            volume = volume.add(quantity);
            tradeCount++;
        }
        
        public void reset(BigDecimal openingPrice) {
            this.open = openingPrice;
            this.high = openingPrice;
            this.low = openingPrice;
            this.close = openingPrice;
            this.volume = BigDecimal.ZERO;
            this.tradeCount = 0;
        }
    }

    /**
     * Market status snapshot
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MarketStatus {
        private String symbol;
        private BigDecimal currentPrice;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private BigDecimal volume;
        private long tradeCount;
        private LocalDateTime timestamp;
    }
}
