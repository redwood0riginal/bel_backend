package com.sypexfs.msin_bourse_enligne.trading.service;

import com.sypexfs.msin_bourse_enligne.trading.dto.*;
import com.sypexfs.msin_bourse_enligne.trading.entity.Order;
import com.sypexfs.msin_bourse_enligne.trading.entity.UserTransaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TradingService {

    // ==================== Order Operations ====================
    
    /**
     * Create a new order
     */
    OrderResponse createOrder(Long userId, OrderRequest request);

    /**
     * Get order by ID
     */
    OrderResponse getOrderById(Long orderId, Long userId);

    /**
     * Get all orders for a user
     */
    List<OrderResponse> getUserOrders(Long userId, int page, int size);

    /**
     * Get active orders (PENDING or PARTIAL) for a user
     */
    List<OrderResponse> getActiveOrders(Long userId);

    /**
     * Get pending orders for a user
     */
    List<OrderResponse> getPendingOrders(Long userId);

    /**
     * Get filled orders for a user
     */
    List<OrderResponse> getFilledOrders(Long userId, int page, int size);

    /**
     * Get cancelled orders for a user
     */
    List<OrderResponse> getCancelledOrders(Long userId, int page, int size);

    /**
     * Get orders by symbol for a user
     */
    List<OrderResponse> getOrdersBySymbol(Long userId, String symbol);

    /**
     * Get buy orders for a user
     */
    List<OrderResponse> getBuyOrders(Long userId, int page, int size);

    /**
     * Get sell orders for a user
     */
    List<OrderResponse> getSellOrders(Long userId, int page, int size);

    /**
     * Get orders by date range
     */
    List<OrderResponse> getOrdersByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Update an existing order
     */
    OrderResponse updateOrder(Long orderId, Long userId, OrderUpdateRequest request);

    /**
     * Cancel an order
     */
    OrderResponse cancelOrder(Long orderId, Long userId, String reason);

    /**
     * Delete an order (soft delete by marking as cancelled)
     */
    void deleteOrder(Long orderId, Long userId);

    /**
     * Get order statistics for a user
     */
    OrderStatistics getOrderStatistics(Long userId);

    /**
     * Check if user has pending orders for a symbol
     */
    boolean hasPendingOrdersForSymbol(Long userId, String symbol);

    // ==================== Transaction Operations ====================

    /**
     * Get all transactions for a user
     */
    List<TransactionResponse> getUserTransactions(Long userId, int page, int size);

    /**
     * Get transaction by ID
     */
    TransactionResponse getTransactionById(Long transactionId, Long userId);

    /**
     * Get transactions by order
     */
    List<TransactionResponse> getTransactionsByOrder(Long orderId, Long userId);

    /**
     * Get transactions by symbol
     */
    List<TransactionResponse> getTransactionsBySymbol(Long userId, String symbol);

    /**
     * Get buy transactions
     */
    List<TransactionResponse> getBuyTransactions(Long userId, int page, int size);

    /**
     * Get sell transactions
     */
    List<TransactionResponse> getSellTransactions(Long userId, int page, int size);

    /**
     * Get transactions by date range
     */
    List<TransactionResponse> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get pending transactions
     */
    List<TransactionResponse> getPendingTransactions(Long userId);

    /**
     * Get settled transactions
     */
    List<TransactionResponse> getSettledTransactions(Long userId, int page, int size);

    // ==================== Order History Operations ====================

    /**
     * Get order history
     */
    List<OrderHistoryResponse> getOrderHistory(Long orderId, Long userId);

    /**
     * Get order history by date range
     */
    List<OrderHistoryResponse> getOrderHistoryByDateRange(Long orderId, Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // ==================== Helper Methods ====================

    /**
     * Convert Order entity to OrderResponse DTO
     */
    OrderResponse convertToOrderResponse(Order order);

    /**
     * Convert UserTransaction entity to TransactionResponse DTO
     */
    TransactionResponse convertToTransactionResponse(UserTransaction transaction);
}
