package com.sypexfs.msin_bourse_enligne.trading.controller;

import com.sypexfs.msin_bourse_enligne.common.dto.ApiResponse;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderBook;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderBookDepth;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderBookStats;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderMatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for live order book operations
 * This is the authoritative source for real-time order book data
 * 
 * Replaces deprecated MarketService orderbook methods
 */
@RestController
@RequestMapping("/orderbook")
@RequiredArgsConstructor
@Slf4j
public class OrderBookController {

    private final OrderMatchingEngine matchingEngine;

    /**
     * Get live order book depth for a symbol
     * Shows top N price levels for bids and asks
     * 
     * @param symbol Stock symbol (e.g., ATW, IAM)
     * @param levels Number of price levels to show (default: 10)
     * @return Order book depth with bids and asks
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<OrderBookDepth>> getOrderBook(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int levels) {
        
        log.debug("Fetching order book for symbol: {} with {} levels", symbol, levels);
        
        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
        if (orderBook == null) {
            return ResponseEntity.ok(ApiResponse.error(
                "Order book not found for symbol: " + symbol,
                "ORDERBOOK_NOT_FOUND"
            ));
        }
        
        OrderBookDepth depth = orderBook.getDepth(levels);
        return ResponseEntity.ok(ApiResponse.success(
            depth,
            "Order book retrieved successfully"
        ));
    }

    /**
     * Get order book statistics for a symbol
     * Includes total orders, volumes, best bid/ask, spread, mid-price
     * 
     * @param symbol Stock symbol
     * @return Order book statistics
     */
    @GetMapping("/{symbol}/stats")
    public ResponseEntity<ApiResponse<OrderBookStats>> getOrderBookStats(
            @PathVariable String symbol) {
        
        log.debug("Fetching order book stats for symbol: {}", symbol);
        
        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
        if (orderBook == null) {
            return ResponseEntity.ok(ApiResponse.error(
                "Order book not found for symbol: " + symbol,
                "ORDERBOOK_NOT_FOUND"
            ));
        }
        
        OrderBookStats stats = orderBook.getStats();
        return ResponseEntity.ok(ApiResponse.success(
            stats,
            "Order book statistics retrieved successfully"
        ));
    }

    /**
     * Get all active symbols with order books
     * 
     * @return List of symbols that have active order books
     */
    @GetMapping("/symbols")
    public ResponseEntity<ApiResponse<List<String>>> getActiveSymbols() {
        log.debug("Fetching all active symbols with order books");
        
        List<String> symbols = matchingEngine.getAllSymbols();
        return ResponseEntity.ok(ApiResponse.success(
            symbols,
            "Active symbols retrieved successfully"
        ));
    }

    /**
     * Get order book depth for multiple symbols
     * Useful for market overview displays
     * 
     * @param symbols Comma-separated list of symbols
     * @param levels Number of price levels per symbol
     * @return Map of symbol to order book depth
     */
    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<List<OrderBookDepth>>> getBatchOrderBooks(
            @RequestParam String symbols,
            @RequestParam(defaultValue = "5") int levels) {
        
        log.debug("Fetching batch order books for symbols: {}", symbols);
        
        String[] symbolArray = symbols.split(",");
        List<OrderBookDepth> depths = java.util.Arrays.stream(symbolArray)
            .map(String::trim)
            .map(symbol -> {
                OrderBook orderBook = matchingEngine.getOrderBook(symbol);
                return orderBook != null ? orderBook.getDepth(levels) : null;
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(
            depths,
            "Batch order books retrieved successfully"
        ));
    }

    /**
     * Get order book summary for all active symbols
     * Returns basic stats for each symbol
     * 
     * @return List of order book statistics for all symbols
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<OrderBookStats>>> getAllOrderBookSummary() {
        log.debug("Fetching order book summary for all symbols");
        
        List<String> symbols = matchingEngine.getAllSymbols();
        List<OrderBookStats> summaries = symbols.stream()
            .map(symbol -> {
                OrderBook orderBook = matchingEngine.getOrderBook(symbol);
                return orderBook != null ? orderBook.getStats() : null;
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(
            summaries,
            "Order book summary retrieved successfully"
        ));
    }

    /**
     * Check if an order book exists for a symbol
     * 
     * @param symbol Stock symbol
     * @return Boolean indicating if order book exists
     */
    @GetMapping("/{symbol}/exists")
    public ResponseEntity<ApiResponse<Boolean>> orderBookExists(
            @PathVariable String symbol) {
        
        log.debug("Checking if order book exists for symbol: {}", symbol);
        
        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
        boolean exists = orderBook != null;
        
        return ResponseEntity.ok(ApiResponse.success(
            exists,
            exists ? "Order book exists" : "Order book does not exist"
        ));
    }
}
