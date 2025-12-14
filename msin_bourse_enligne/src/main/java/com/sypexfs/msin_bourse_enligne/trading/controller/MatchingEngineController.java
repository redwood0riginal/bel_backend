package com.sypexfs.msin_bourse_enligne.trading.controller;

import com.sypexfs.msin_bourse_enligne.trading.matching.*;
import com.sypexfs.msin_bourse_enligne.trading.service.OrderExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for matching engine operations
 */
@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MatchingEngineController {

    private final OrderMatchingEngine matchingEngine;
    private final MarketDataService marketDataService;
    private final OrderExecutionService executionService;

    /**
     * Get order book for a symbol
     * GET /api/matching/orderbook/{symbol}
     */
    @GetMapping("/orderbook/{symbol}")
    public ResponseEntity<OrderBookDepth> getOrderBook(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int levels) {
        log.info("Fetching order book for symbol: {}", symbol);
        
        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
        if (orderBook == null) {
            return ResponseEntity.notFound().build();
        }
        
        OrderBookDepth depth = orderBook.getDepth(levels);
        return ResponseEntity.ok(depth);
    }

    /**
     * Get order book statistics
     * GET /api/matching/orderbook/{symbol}/stats
     */
    @GetMapping("/orderbook/{symbol}/stats")
    public ResponseEntity<OrderBookStats> getOrderBookStats(@PathVariable String symbol) {
        log.info("Fetching order book stats for symbol: {}", symbol);
        
        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
        if (orderBook == null) {
            return ResponseEntity.notFound().build();
        }
        
        OrderBookStats stats = orderBook.getStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get market status for a symbol
     * GET /api/matching/market/{symbol}
     */
    @GetMapping("/market/{symbol}")
    public ResponseEntity<MarketDataService.MarketStatus> getMarketStatus(@PathVariable String symbol) {
        log.info("Fetching market status for symbol: {}", symbol);
        
        MarketDataService.MarketStatus status = marketDataService.getMarketStatus(symbol);
        return ResponseEntity.ok(status);
    }

    /**
     * Get order execution status
     * GET /api/matching/execution/{orderId}
     */
    @GetMapping("/execution/{orderId}")
    public ResponseEntity<OrderExecutionService.OrderExecutionStatus> getExecutionStatus(
            @PathVariable Long orderId,
            Authentication authentication) {
        log.info("Fetching execution status for order: {}", orderId);
        
        Long userId = getUserIdFromAuth(authentication);
        OrderExecutionService.OrderExecutionStatus status = 
            executionService.getExecutionStatus(orderId, userId);
        
        return ResponseEntity.ok(status);
    }

    /**
     * Trigger stop order check for a symbol (admin only)
     * POST /api/matching/check-stops/{symbol}
     */
    @PostMapping("/check-stops/{symbol}")
    public ResponseEntity<String> checkStopOrders(@PathVariable String symbol) {
        log.info("Manually triggering stop order check for symbol: {}", symbol);
        
        matchingEngine.checkStopOrders(symbol);
        return ResponseEntity.ok("Stop orders checked for symbol: " + symbol);
    }

    /**
     * Reset daily statistics (admin only)
     * POST /api/matching/reset-daily-stats
     */
    @PostMapping("/reset-daily-stats")
    public ResponseEntity<String> resetDailyStats() {
        log.info("Manually resetting daily statistics");
        
        marketDataService.resetDailyStats();
        return ResponseEntity.ok("Daily statistics reset successfully");
    }

    /**
     * Get current price for a symbol
     * GET /api/matching/price/{symbol}
     */
    @GetMapping("/price/{symbol}")
    public ResponseEntity<java.math.BigDecimal> getCurrentPrice(@PathVariable String symbol) {
        log.info("Fetching current price for symbol: {}", symbol);
        
        java.math.BigDecimal price = marketDataService.getCurrentPrice(symbol);
        return ResponseEntity.ok(price);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        // TODO: Extract user ID from authentication principal
        return 1L; // Placeholder
    }
}
