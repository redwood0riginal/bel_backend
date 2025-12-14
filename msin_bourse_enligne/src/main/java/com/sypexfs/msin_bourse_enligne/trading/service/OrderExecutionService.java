package com.sypexfs.msin_bourse_enligne.trading.service;

import com.sypexfs.msin_bourse_enligne.trading.dto.OrderRequest;
import com.sypexfs.msin_bourse_enligne.trading.dto.OrderResponse;
import com.sypexfs.msin_bourse_enligne.trading.entity.Order;
import com.sypexfs.msin_bourse_enligne.trading.matching.MatchingResult;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderMatchingEngine;
import com.sypexfs.msin_bourse_enligne.trading.repository.OrderRepository;
import com.sypexfs.msin_bourse_enligne.trading.websocket.TradingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for executing orders through the matching engine
 * Integrates with WebSocket for real-time updates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExecutionService {

    private final OrderMatchingEngine matchingEngine;
    private final OrderRepository orderRepository;
    private final TradingService tradingService;
    private final TradingWebSocketHandler webSocketHandler;

    /**
     * Submit order for execution
     */
    @Transactional
    public OrderResponse submitOrder(Long userId, OrderRequest request) {
        log.info("Submitting order for user: {} symbol: {}", userId, request.getSymbol());
        
        // Create order entity
        Order order = createOrderFromRequest(userId, request);
        order = orderRepository.save(order);
        
        // Process through matching engine
        MatchingResult result = matchingEngine.processOrder(order);
        
        // Send real-time updates
        sendOrderUpdate(order, result);
        
        // Convert to response
        return tradingService.convertToOrderResponse(order);
    }

    /**
     * Cancel order and remove from order book
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId, String reason) {
        log.info("Cancelling order: {} for user: {}", orderId, userId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }
        
        if ("FILLED".equals(order.getStatId())) {
            throw new RuntimeException("Cannot cancel filled order");
        }
        
        // Remove from order book if pending
        if ("PENDING".equals(order.getStatId()) || "PARTIAL".equals(order.getStatId())) {
            var orderBook = matchingEngine.getOrderBook(order.getSymbol());
            if (orderBook != null) {
                orderBook.removeOrder(orderId);
            }
        }
        
        // Update order status
        order.setStatId("CANCELLED");
        order = orderRepository.save(order);
        
        // Send real-time update
        sendOrderCancellation(order, reason);
        
        return tradingService.convertToOrderResponse(order);
    }

    /**
     * Modify pending order
     */
    @Transactional
    public OrderResponse modifyOrder(Long orderId, Long userId, BigDecimal newPrice, BigDecimal newQuantity) {
        log.info("Modifying order: {} for user: {}", orderId, userId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }
        
        if (!"PENDING".equals(order.getStatId())) {
            throw new RuntimeException("Can only modify pending orders");
        }
        
        // Remove from order book
        var orderBook = matchingEngine.getOrderBook(order.getSymbol());
        if (orderBook != null) {
            orderBook.removeOrder(orderId);
        }
        
        // Update order
        if (newPrice != null) {
            order.setPrice(newPrice);
        }
        if (newQuantity != null) {
            order.setQuantity(newQuantity);
            order.setOrderAmount(order.getPrice().multiply(newQuantity));
        }
        
        order = orderRepository.save(order);
        
        // Resubmit to matching engine
        MatchingResult result = matchingEngine.processOrder(order);
        
        // Send real-time update
        sendOrderUpdate(order, result);
        
        return tradingService.convertToOrderResponse(order);
    }

    /**
     * Get order execution status
     */
    public OrderExecutionStatus getExecutionStatus(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }
        
        return new OrderExecutionStatus(
            order.getId(),
            order.getSymbol(),
            order.getStatId(),
            order.getQuantity(),
            order.getExecQty(),
            order.getRemainingQuantity(),
            order.getPrice(),
            order.getExecAvgPrice(),
            order.getDateEntry(),
            order.getUpdatedAt()
        );
    }

    /**
     * Create order entity from request
     */
    private Order createOrderFromRequest(Long userId, OrderRequest request) {
        Order order = new Order();
        order.setUserId(userId);
        order.setSign(request.getSign());
        order.setPrice(request.getPrice());
        order.setStopPrice(request.getStopPrice());
        order.setDisplayedQuantity(request.getDisplayedQuantity());
        order.setQuantity(request.getQuantity());
        order.setDateOrder(request.getDateOrder());
        order.setDateExpiry(request.getDateExpiry());
        order.setTimeOrder(request.getTimeOrder());
        order.setTimeExpiry(request.getTimeExpiry());
        order.setExpiryTypeId(request.getExpiryTypeId());
        order.setStatId("PENDING");
        order.setOrderTypeId(request.getOrderTypeId());
        order.setBrokerId(request.getBrokerId());
        order.setCashAccountId(request.getCashAccountId());
        order.setEntityId(request.getEntityId());
        order.setPortfId(request.getPortfId());
        order.setSubPortfId(request.getSubPortfId());
        order.setSecAccountId(request.getSecAccountId());
        order.setSecId(request.getSecId());
        order.setSymbol(request.getSymbol());
        order.setSubRedTypeId(request.getSubRedTypeId());
        order.setExternalRef(request.getExternalRef());
        order.setClassId(request.getClassId());
        order.setAccountType(request.getAccountType());
        order.setDateEntry(LocalDateTime.now());
        order.setExecQty(BigDecimal.ZERO);
        
        if (request.getPrice() != null && request.getQuantity() != null) {
            order.setOrderAmount(request.getPrice().multiply(request.getQuantity()));
        }
        
        return order;
    }

    /**
     * Send order update via WebSocket
     */
    private void sendOrderUpdate(Order order, MatchingResult result) {
        try {
            webSocketHandler.sendOrderUpdate(order, result);
        } catch (Exception e) {
            log.error("Failed to send order update via WebSocket", e);
        }
    }

    /**
     * Send order cancellation via WebSocket
     */
    private void sendOrderCancellation(Order order, String reason) {
        try {
            webSocketHandler.sendOrderCancellation(order, reason);
        } catch (Exception e) {
            log.error("Failed to send order cancellation via WebSocket", e);
        }
    }

    /**
     * Order execution status DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class OrderExecutionStatus {
        private Long orderId;
        private String symbol;
        private String status;
        private BigDecimal totalQuantity;
        private BigDecimal executedQuantity;
        private BigDecimal remainingQuantity;
        private BigDecimal orderPrice;
        private BigDecimal avgExecutionPrice;
        private LocalDateTime submittedAt;
        private LocalDateTime lastUpdatedAt;
    }
}
