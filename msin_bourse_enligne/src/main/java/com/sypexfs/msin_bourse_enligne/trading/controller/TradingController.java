package com.sypexfs.msin_bourse_enligne.trading.controller;

import com.sypexfs.msin_bourse_enligne.trading.dto.*;
import com.sypexfs.msin_bourse_enligne.trading.service.TradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/trading")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TradingController {

    private final TradingService tradingService;

    // ==================== Order Endpoints ====================

    /**
     * Create a new order
     * POST /api/trading/orders
     */
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        log.info("Creating order for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        OrderResponse response = tradingService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get order by ID
     * GET /api/trading/orders/{orderId}
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {
        log.info("Fetching order: {} for user: {}", orderId, authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        OrderResponse response = tradingService.getOrderById(orderId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all orders for authenticated user
     * GET /api/trading/orders?page=0&size=20
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching orders for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getUserOrders(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active orders (PENDING or PARTIAL)
     * GET /api/trading/orders/active
     */
    @GetMapping("/orders/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(Authentication authentication) {
        log.info("Fetching active orders for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getActiveOrders(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending orders
     * GET /api/trading/orders/pending
     */
    @GetMapping("/orders/pending")
    public ResponseEntity<List<OrderResponse>> getPendingOrders(Authentication authentication) {
        log.info("Fetching pending orders for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getPendingOrders(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get filled orders
     * GET /api/trading/orders/filled?page=0&size=20
     */
    @GetMapping("/orders/filled")
    public ResponseEntity<List<OrderResponse>> getFilledOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching filled orders for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getFilledOrders(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get cancelled orders
     * GET /api/trading/orders/cancelled?page=0&size=20
     */
    @GetMapping("/orders/cancelled")
    public ResponseEntity<List<OrderResponse>> getCancelledOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching cancelled orders for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getCancelledOrders(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get orders by symbol
     * GET /api/trading/orders/symbol/{symbol}
     */
    @GetMapping("/orders/symbol/{symbol}")
    public ResponseEntity<List<OrderResponse>> getOrdersBySymbol(
            @PathVariable String symbol,
            Authentication authentication) {
        log.info("Fetching orders for symbol: {} and user: {}", symbol, authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getOrdersBySymbol(userId, symbol);
        return ResponseEntity.ok(response);
    }

    /**
     * Get buy orders
     * GET /api/trading/orders/buy?page=0&size=20
     */
    @GetMapping("/orders/buy")
    public ResponseEntity<List<OrderResponse>> getBuyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching buy orders for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getBuyOrders(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get sell orders
     * GET /api/trading/orders/sell?page=0&size=20
     */
    @GetMapping("/orders/sell")
    public ResponseEntity<List<OrderResponse>> getSellOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching sell orders for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getSellOrders(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get orders by date range
     * GET /api/trading/orders/range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
     */
    @GetMapping("/orders/range")
    public ResponseEntity<List<OrderResponse>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        log.info("Fetching orders for user: {} between {} and {}", authentication.getName(), startDate, endDate);
        Long userId = getUserIdFromAuth(authentication);
        List<OrderResponse> response = tradingService.getOrdersByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an order
     * PUT /api/trading/orders/{orderId}
     */
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateRequest request,
            Authentication authentication) {
        log.info("Updating order: {} for user: {}", orderId, authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        OrderResponse response = tradingService.updateOrder(orderId, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an order
     * POST /api/trading/orders/{orderId}/cancel
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("Cancelling order: {} for user: {}", orderId, authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        OrderResponse response = tradingService.cancelOrder(orderId, userId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an order
     * DELETE /api/trading/orders/{orderId}
     */
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        log.info("Deleting order: {} for user: {}", orderId, authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        tradingService.deleteOrder(orderId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get order statistics
     * GET /api/trading/orders/statistics
     */
    @GetMapping("/orders/statistics")
    public ResponseEntity<OrderStatistics> getOrderStatistics(Authentication authentication) {
        log.info("Fetching order statistics for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        OrderStatistics response = tradingService.getOrderStatistics(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user has pending orders for symbol
     * GET /api/trading/orders/has-pending/{symbol}
     */
    @GetMapping("/orders/has-pending/{symbol}")
    public ResponseEntity<Boolean> hasPendingOrdersForSymbol(
            @PathVariable String symbol,
            Authentication authentication) {
        log.info("Checking pending orders for symbol: {} and user: {}", symbol, authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        boolean hasPending = tradingService.hasPendingOrdersForSymbol(userId, symbol);
        return ResponseEntity.ok(hasPending);
    }

    // ==================== Transaction Endpoints ====================

    /**
     * Get all transactions for authenticated user
     * GET /api/trading/transactions?page=0&size=20
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching transactions for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<TransactionResponse> response = tradingService.getUserTransactions(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction by ID
     * GET /api/trading/transactions/{transactionId}
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long transactionId,
            Authentication authentication) {
        log.info("Fetching transaction: {} for user: {}", transactionId, authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        TransactionResponse response = tradingService.getTransactionById(transactionId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transactions by order
     * GET /api/trading/transactions/order/{orderId}
     */
    @GetMapping("/transactions/order/{orderId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        log.info("Fetching transactions for order: {}", orderId);
        Long userId = getUserIdFromAuth(authentication);
        List<TransactionResponse> response = tradingService.getTransactionsByOrder(orderId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transactions by symbol
     * GET /api/trading/transactions/symbol/{symbol}
     */
    @GetMapping("/transactions/symbol/{symbol}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsBySymbol(
            @PathVariable String symbol,
            Authentication authentication) {
        log.info("Fetching transactions for symbol: {} and user: {}", symbol, authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<TransactionResponse> response = tradingService.getTransactionsBySymbol(userId, symbol);
        return ResponseEntity.ok(response);
    }

    /**
     * Get buy transactions
     * GET /api/trading/transactions/buy?page=0&size=20
     */
    @GetMapping("/transactions/buy")
    public ResponseEntity<List<TransactionResponse>> getBuyTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching buy transactions for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<TransactionResponse> response = tradingService.getBuyTransactions(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get sell transactions
     * GET /api/trading/transactions/sell?page=0&size=20
     */
    @GetMapping("/transactions/sell")
    public ResponseEntity<List<TransactionResponse>> getSellTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching sell transactions for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<TransactionResponse> response = tradingService.getSellTransactions(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transactions by date range
     * GET /api/trading/transactions/range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
     */
    @GetMapping("/transactions/range")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        log.info("Fetching transactions for user: {} between {} and {}", authentication.getName(), startDate, endDate);
        Long userId = getUserIdFromAuth(authentication);
        List<TransactionResponse> response = tradingService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending transactions
     * GET /api/trading/transactions/pending
     */
    @GetMapping("/transactions/pending")
    public ResponseEntity<List<TransactionResponse>> getPendingTransactions(Authentication authentication) {
        log.info("Fetching pending transactions for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<TransactionResponse> response = tradingService.getPendingTransactions(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get settled transactions
     * GET /api/trading/transactions/settled?page=0&size=20
     */
    @GetMapping("/transactions/settled")
    public ResponseEntity<List<TransactionResponse>> getSettledTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Fetching settled transactions for user: {}", authentication.getName());
        Long userId = getUserIdFromAuth(authentication);
        List<TransactionResponse> response = tradingService.getSettledTransactions(userId, page, size);
        return ResponseEntity.ok(response);
    }

    // ==================== Order History Endpoints ====================

    /**
     * Get order history
     * GET /api/trading/orders/{orderId}/history
     */
    @GetMapping("/orders/{orderId}/history")
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory(
            @PathVariable Long orderId,
            Authentication authentication) {
        log.info("Fetching order history for order: {}", orderId);
        Long userId = getUserIdFromAuth(authentication);
        List<OrderHistoryResponse> response = tradingService.getOrderHistory(orderId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get order history by date range
     * GET /api/trading/orders/{orderId}/history/range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
     */
    @GetMapping("/orders/{orderId}/history/range")
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistoryByDateRange(
            @PathVariable Long orderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        log.info("Fetching order history for order: {} between {} and {}", orderId, startDate, endDate);
        Long userId = getUserIdFromAuth(authentication);
        List<OrderHistoryResponse> response = tradingService.getOrderHistoryByDateRange(orderId, userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // ==================== Helper Methods ====================

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.sypexfs.msin_bourse_enligne.auth.security.UserDetailsImpl) {
            com.sypexfs.msin_bourse_enligne.auth.security.UserDetailsImpl userDetails = 
                (com.sypexfs.msin_bourse_enligne.auth.security.UserDetailsImpl) principal;
            return userDetails.getId();
        }
        
        throw new RuntimeException("Invalid authentication principal type");
    }
}
