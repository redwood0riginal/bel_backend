package com.sypexfs.msin_bourse_enligne.trading.matching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler for matching engine tasks
 * - Loads pending orders on startup
 * - Checks stop orders periodically
 * - Resets daily statistics at market open
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEngineScheduler {

    private final OrderMatchingEngine matchingEngine;
    private final MarketDataService marketDataService;

    /**
     * Initialize matching engine on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Initializing matching engine on startup");
        
        // Load pending orders from database
        matchingEngine.loadPendingOrders();
        
        // Initialize market data cache
        marketDataService.initializeMarketData();
        
        log.info("Matching engine initialization complete");
    }

    /**
     * Check stop orders every 5 seconds
     */
    @Scheduled(fixedDelay = 5000)
    public void checkStopOrders() {
        try {
            // Get all symbols with orders
            List<String> symbols = getActiveSymbols();
            
            if (symbols == null || symbols.isEmpty()) {
                log.debug("No active order books to check for stop orders");
                return;
            }
            
            for (String symbol : symbols) {
                matchingEngine.checkStopOrders(symbol);
            }
        } catch (Exception e) {
            log.error("Error checking stop orders", e);
        }
    }

    /**
     * Reset daily statistics at market open (9:00 AM)
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void resetDailyStatistics() {
        log.info("Resetting daily statistics at market open");
        try {
            marketDataService.resetDailyStats();
        } catch (Exception e) {
            log.error("Error resetting daily statistics", e);
        }
    }

    /**
     * Log matching engine statistics every hour
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void logStatistics() {
        try {
            List<String> symbols = getActiveSymbols();
            
            if (symbols == null || symbols.isEmpty()) {
                log.debug("No active order books for statistics");
                return;
            }
            
            log.info("=== Matching Engine Statistics ===");
            
            for (String symbol : symbols) {
                OrderBook orderBook = matchingEngine.getOrderBook(symbol);
                if (orderBook != null) {
                    OrderBookStats stats = orderBook.getStats();
                    log.info("Symbol: {} - Buy Orders: {}, Sell Orders: {}, Best Bid: {}, Best Ask: {}, Spread: {}",
                             stats.getSymbol(),
                             stats.getTotalBuyOrders(),
                             stats.getTotalSellOrders(),
                             stats.getBestBid(),
                             stats.getBestAsk(),
                             stats.getSpread());
                }
            }
        } catch (Exception e) {
            log.error("Error logging statistics", e);
        }
    }

    /**
     * Get list of active symbols
     */
    private List<String> getActiveSymbols() {
        // Get all symbols from the matching engine
        return matchingEngine.getAllSymbols();
    }
}
