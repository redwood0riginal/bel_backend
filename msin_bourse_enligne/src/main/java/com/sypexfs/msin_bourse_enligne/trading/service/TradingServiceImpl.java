package com.sypexfs.msin_bourse_enligne.trading.service;

import com.sypexfs.msin_bourse_enligne.trading.dto.*;
import com.sypexfs.msin_bourse_enligne.trading.entity.Order;
import com.sypexfs.msin_bourse_enligne.trading.entity.OrderHistory;
import com.sypexfs.msin_bourse_enligne.trading.entity.UserTransaction;
import com.sypexfs.msin_bourse_enligne.trading.matching.MatchingResult;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderMatchingEngine;
import com.sypexfs.msin_bourse_enligne.trading.repository.OrderHistoryRepository;
import com.sypexfs.msin_bourse_enligne.trading.repository.OrderRepository;
import com.sypexfs.msin_bourse_enligne.trading.repository.UserTransactionRepository;
import com.sypexfs.msin_bourse_enligne.portfolio.service.PortfolioService;
import com.sypexfs.msin_bourse_enligne.portfolio.dto.PortfolioResponse;
import com.sypexfs.msin_bourse_enligne.portfolio.dto.PositionResponse;
import com.sypexfs.msin_bourse_enligne.market.websocket.MarketWebSocketHandler;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketOrderbook;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketOrderbookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TradingServiceImpl implements TradingService {

    private final OrderRepository orderRepository;
    private final UserTransactionRepository transactionRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderMatchingEngine matchingEngine;
    private final PortfolioService portfolioService;
    private final MarketWebSocketHandler webSocketHandler;
    private final MarketService marketService;
    private final MarketMapper marketMapper;
    private final MarketOrderbookRepository marketOrderbookRepository;

    // ==================== Order Operations ====================

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        log.info("Creating order for user: {} with symbol: {}", userId, request.getSymbol());
        
        // Validate portfolio before creating order
        // TEMPORARILY DISABLED FOR TESTING - UNCOMMENT IN PRODUCTION
        // validatePortfolioForOrder(userId, request);

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

        // Calculate order amount
        if (request.getPrice() != null && request.getQuantity() != null) {
            order.setOrderAmount(request.getPrice().multiply(request.getQuantity()));
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // Create order history entry
        createOrderHistoryEntry(savedOrder, null, "PENDING", "Order created");

        // Process order through matching engine
        try {
            MatchingResult result = matchingEngine.processOrder(savedOrder);
            log.info("Order {} processed by matching engine. Status: {}, Executions: {}", 
                     savedOrder.getId(), result.getOrder().getStatId(), result.getExecutions().size());
            
            // Reload order to get updated status from matching engine
            savedOrder = orderRepository.findById(savedOrder.getId())
                .orElse(savedOrder);
        } catch (Exception e) {
            log.error("Error processing order through matching engine: {}", e.getMessage(), e);
            // Order is still saved, just not matched yet
        }

        // Sync order to market orderbook if still pending/partial
        if ("PENDING".equals(savedOrder.getStatId()) || "PARTIAL".equals(savedOrder.getStatId())) {
            syncOrderToMarketOrderbook(savedOrder);
        } else if ("FILLED".equals(savedOrder.getStatId())) {
            // Remove filled orders from market orderbook
            removeOrderFromMarketOrderbook(savedOrder);
        }

        // Broadcast orderbook update via WebSocket
        broadcastOrderbookUpdate(request.getSymbol());

        return convertToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId, Long userId) {
        log.debug("Fetching order: {} for user: {}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }

        return convertToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getUserOrders(Long userId, int page, int size) {
        log.debug("Fetching orders for user: {} (page: {}, size: {})", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findRecentOrdersByUser(userId, pageable).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getActiveOrders(Long userId) {
        log.debug("Fetching active orders for user: {}", userId);
        return orderRepository.findActiveOrdersByUser(userId).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getPendingOrders(Long userId) {
        log.debug("Fetching pending orders for user: {}", userId);
        return orderRepository.findPendingOrdersByUser(userId).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getFilledOrders(Long userId, int page, int size) {
        log.debug("Fetching filled orders for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findFilledOrdersByUser(userId, pageable).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getCancelledOrders(Long userId, int page, int size) {
        log.debug("Fetching cancelled orders for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findCancelledOrdersByUser(userId, pageable).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersBySymbol(Long userId, String symbol) {
        log.debug("Fetching orders for user: {} and symbol: {}", userId, symbol);
        return orderRepository.findByUserIdAndSymbol(userId, symbol).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getBuyOrders(Long userId, int page, int size) {
        log.debug("Fetching buy orders for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findBuyOrdersByUser(userId, pageable).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getSellOrders(Long userId, int page, int size) {
        log.debug("Fetching sell orders for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findSellOrdersByUser(userId, pageable).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching orders for user: {} between {} and {}", userId, startDate, endDate);
        return orderRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long orderId, Long userId, OrderUpdateRequest request) {
        log.info("Updating order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }

        if (!"PENDING".equals(order.getStatId())) {
            throw new RuntimeException("Cannot update order with status: " + order.getStatId());
        }

        String previousStatus = order.getStatId();
        BigDecimal previousExecQty = order.getExecQty();

        // Update fields
        if (request.getPrice() != null) {
            order.setPrice(request.getPrice());
        }
        if (request.getStopPrice() != null) {
            order.setStopPrice(request.getStopPrice());
        }
        if (request.getDisplayedQuantity() != null) {
            order.setDisplayedQuantity(request.getDisplayedQuantity());
        }
        if (request.getQuantity() != null) {
            order.setQuantity(request.getQuantity());
        }
        if (request.getDateExpiry() != null) {
            order.setDateExpiry(request.getDateExpiry());
        }
        if (request.getTimeExpiry() != null) {
            order.setTimeExpiry(request.getTimeExpiry());
        }
        if (request.getExpiryTypeId() != null) {
            order.setExpiryTypeId(request.getExpiryTypeId());
        }

        // Recalculate order amount
        if (order.getPrice() != null && order.getQuantity() != null) {
            order.setOrderAmount(order.getPrice().multiply(order.getQuantity()));
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order updated successfully: {}", orderId);

        // Create order history entry
        createOrderHistoryEntry(updatedOrder, previousStatus, updatedOrder.getStatId(), "Order updated");

        // Broadcast orderbook update via WebSocket
        broadcastOrderbookUpdate(updatedOrder.getSymbol());

        return convertToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId, String reason) {
        log.info("Cancelling order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }

        if ("FILLED".equals(order.getStatId()) || "CANCELLED".equals(order.getStatId())) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatId());
        }

        String previousStatus = order.getStatId();
        order.setStatId("CANCELLED");

        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: {}", orderId);

        // Create order history entry
        createOrderHistoryEntry(cancelledOrder, previousStatus, "CANCELLED", reason != null ? reason : "Order cancelled by user");

        // Remove from market orderbook
        removeOrderFromMarketOrderbook(cancelledOrder);

        // Broadcast orderbook update via WebSocket
        broadcastOrderbookUpdate(cancelledOrder.getSymbol());

        return convertToOrderResponse(cancelledOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        log.info("Deleting order: {} for user: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }

        // Soft delete by marking as cancelled
        if (!"CANCELLED".equals(order.getStatId())) {
            cancelOrder(orderId, userId, "Order deleted");
        }
    }

    @Override
    public OrderStatistics getOrderStatistics(Long userId) {
        log.debug("Calculating order statistics for user: {}", userId);

        List<Order> allOrders = orderRepository.findByUserId(userId);

        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(Order::isPending).count();
        long filledOrders = allOrders.stream().filter(Order::isFilled).count();
        long cancelledOrders = allOrders.stream().filter(Order::isCancelled).count();
        long partialOrders = allOrders.stream().filter(Order::isPartiallyFilled).count();
        long rejectedOrders = allOrders.stream().filter(Order::isRejected).count();

        long totalBuyOrders = allOrders.stream().filter(Order::isBuyOrder).count();
        long totalSellOrders = allOrders.stream().filter(Order::isSellOrder).count();

        BigDecimal totalBuyAmount = allOrders.stream()
                .filter(Order::isBuyOrder)
                .filter(Order::isFilled)
                .map(o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSellAmount = allOrders.stream()
                .filter(Order::isSellOrder)
                .filter(Order::isFilled)
                .map(o -> o.getOrderAmount() != null ? o.getOrderAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderStatistics.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .filledOrders(filledOrders)
                .cancelledOrders(cancelledOrders)
                .partialOrders(partialOrders)
                .rejectedOrders(rejectedOrders)
                .totalBuyOrders(totalBuyOrders)
                .totalSellOrders(totalSellOrders)
                .totalBuyAmount(totalBuyAmount)
                .totalSellAmount(totalSellAmount)
                .build();
    }

    @Override
    public boolean hasPendingOrdersForSymbol(Long userId, String symbol) {
        return orderRepository.hasPendingOrdersForSymbol(userId, symbol);
    }

    // ==================== Transaction Operations ====================

    @Override
    public List<TransactionResponse> getUserTransactions(Long userId, int page, int size) {
        log.debug("Fetching transactions for user: {} (page: {}, size: {})", userId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable).stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId, Long userId) {
        log.debug("Fetching transaction: {} for user: {}", transactionId, userId);
        UserTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        if (!transaction.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to transaction: " + transactionId);
        }

        return convertToTransactionResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getTransactionsByOrder(Long orderId, Long userId) {
        log.debug("Fetching transactions for order: {}", orderId);
        return transactionRepository.findByOrderId(orderId).stream()
                .filter(t -> t.getUserId().equals(userId))
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsBySymbol(Long userId, String symbol) {
        log.debug("Fetching transactions for user: {} and symbol: {}", userId, symbol);
        return transactionRepository.findByUserIdAndSymbol(userId, symbol).stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getBuyTransactions(Long userId, int page, int size) {
        log.debug("Fetching buy transactions for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findBuyTransactionsByUser(userId, pageable).stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getSellTransactions(Long userId, int page, int size) {
        log.debug("Fetching sell transactions for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findSellTransactionsByUser(userId, pageable).stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching transactions for user: {} between {} and {}", userId, startDate, endDate);
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getPendingTransactions(Long userId) {
        log.debug("Fetching pending transactions for user: {}", userId);
        return transactionRepository.findPendingTransactionsByUser(userId).stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getSettledTransactions(Long userId, int page, int size) {
        log.debug("Fetching settled transactions for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findSettledTransactionsByUser(userId, pageable).stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    // ==================== Order History Operations ====================

    @Override
    public List<OrderHistoryResponse> getOrderHistory(Long orderId, Long userId) {
        log.debug("Fetching order history for order: {}", orderId);

        // Verify order belongs to user
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }

        return orderHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId).stream()
                .map(this::convertToOrderHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderHistoryResponse> getOrderHistoryByDateRange(Long orderId, Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching order history for order: {} between {} and {}", orderId, startDate, endDate);

        // Verify order belongs to user
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to order: " + orderId);
        }

        return orderHistoryRepository.findByOrderIdAndDateRange(orderId, startDate, endDate).stream()
                .map(this::convertToOrderHistoryResponse)
                .collect(Collectors.toList());
    }

    // ==================== Helper Methods ====================

    @Override
    public OrderResponse convertToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .sign(order.getSign())
                .price(order.getPrice())
                .stopPrice(order.getStopPrice())
                .displayedQuantity(order.getDisplayedQuantity())
                .quantity(order.getQuantity())
                .orderAmount(order.getOrderAmount())
                .dateOrder(order.getDateOrder())
                .dateExpiry(order.getDateExpiry())
                .timeOrder(order.getTimeOrder())
                .timeExpiry(order.getTimeExpiry())
                .dateEntry(order.getDateEntry())
                .execQty(order.getExecQty())
                .execAvgPrice(order.getExecAvgPrice())
                .expiryTypeId(order.getExpiryTypeId())
                .statId(order.getStatId())
                .orderTypeId(order.getOrderTypeId())
                .brokerId(order.getBrokerId())
                .cashAccountId(order.getCashAccountId())
                .entityId(order.getEntityId())
                .portfId(order.getPortfId())
                .subPortfId(order.getSubPortfId())
                .secAccountId(order.getSecAccountId())
                .secId(order.getSecId())
                .symbol(order.getSymbol())
                .subRedTypeId(order.getSubRedTypeId())
                .externalRef(order.getExternalRef())
                .classId(order.getClassId())
                .accountType(order.getAccountType())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .remainingQuantity(order.getRemainingQuantity())
                .orderSide(order.isBuyOrder() ? "BUY" : "SELL")
                .build();
    }

    @Override
    public TransactionResponse convertToTransactionResponse(UserTransaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .orderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null)
                .userId(transaction.getUserId())
                .symbol(transaction.getSymbol())
                .side(transaction.getSide())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .amount(transaction.getAmount())
                .commission(transaction.getCommission())
                .tax(transaction.getTax())
                .netAmount(transaction.getNetAmount())
                .transactionDate(transaction.getTransactionDate())
                .settlementDate(transaction.getSettlementDate())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private OrderHistoryResponse convertToOrderHistoryResponse(OrderHistory history) {
        return OrderHistoryResponse.builder()
                .id(history.getId())
                .orderId(history.getOrder().getId())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .previousExecQty(history.getPreviousExecQty())
                .newExecQty(history.getNewExecQty())
                .changeReason(history.getChangeReason())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .build();
    }

    @Transactional
    private void createOrderHistoryEntry(Order order, String previousStatus, String newStatus, String reason) {
        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setPreviousExecQty(BigDecimal.ZERO);
        history.setNewExecQty(order.getExecQty());
        history.setChangeReason(reason);
        history.setChangedBy(order.getUserId());
        orderHistoryRepository.save(history);
    }
    
    /**
     * Validate portfolio before placing order
     * Ensures user has sufficient cash for BUY orders and sufficient quantity for SELL orders
     */
    private void validatePortfolioForOrder(Long userId, OrderRequest request) {
        try {
            // Get user's active portfolio
            PortfolioResponse portfolio = portfolioService.getUserActivePortfolio(userId);
            
            boolean isBuyOrder = request.getSign() != null && request.getSign() == 1; // 1 = BUY, -1 = SELL
            
            if (isBuyOrder) {
                // Validate cash balance for BUY orders
                BigDecimal requiredAmount = request.getQuantity()
                    .multiply(request.getPrice())
                    .multiply(BigDecimal.valueOf(1.004)); // Add 0.4% for fees (0.3% commission + 0.1% tax)
                
                if (portfolio.getCashBalance().compareTo(requiredAmount) < 0) {
                    throw new RuntimeException(String.format(
                        "Insufficient cash balance. Required: %.2f MAD, Available: %.2f MAD",
                        requiredAmount, portfolio.getCashBalance()
                    ));
                }
                
                log.info("Portfolio validation passed for BUY order. Required: {}, Available: {}", 
                         requiredAmount, portfolio.getCashBalance());
            } else {
                // Validate position quantity for SELL orders
                try {
                    PositionResponse position = portfolioService.getPosition(
                        portfolio.getId(), 
                        request.getSymbol()
                    );
                    
                    if (position.getQuantity().compareTo(request.getQuantity()) < 0) {
                        throw new RuntimeException(String.format(
                            "Insufficient quantity to sell. Required: %.2f, Available: %.2f",
                            request.getQuantity(), position.getQuantity()
                        ));
                    }
                    
                    log.info("Portfolio validation passed for SELL order. Required: {}, Available: {}", 
                             request.getQuantity(), position.getQuantity());
                } catch (RuntimeException e) {
                    if (e.getMessage().contains("Position not found")) {
                        throw new RuntimeException("No position found for symbol: " + request.getSymbol());
                    }
                    throw e;
                }
            }
        } catch (RuntimeException e) {
            // If no active portfolio found, create one automatically with zero balance
            if (e.getMessage().contains("No active portfolio found")) {
                log.warn("No active portfolio found for user {}. Order will be placed but may fail execution.", userId);
                // Allow order to be placed - it will fail during execution if insufficient funds
            } else {
                log.error("Portfolio validation failed for user {}: {}", userId, e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Sync order to market orderbook table for display
     */
    private void syncOrderToMarketOrderbook(Order order) {
        try {
            log.info("Syncing order {} to market orderbook", order.getId());
            
            // Check if orderbook entry already exists for this order
            String orderMarketId = "ORDER_" + order.getId();
            MarketOrderbook existingEntry = marketOrderbookRepository
                .findByOrderMarketId(orderMarketId)
                .orElse(null);
            
            if (existingEntry != null) {
                // Update existing entry with remaining quantity
                existingEntry.setQuantity(order.getRemainingQuantity());
                existingEntry.setPrice(order.getPrice());
                existingEntry.setDelete(false);
                marketOrderbookRepository.save(existingEntry);
                log.info("Updated market orderbook entry for order {} with remaining qty: {}", 
                         order.getId(), order.getRemainingQuantity());
            } else {
                // Create new entry with remaining quantity
                MarketOrderbook marketOrder = new MarketOrderbook();
                marketOrder.setSecId(order.getSecId() != null ? order.getSecId().toString() : null);
                marketOrder.setSymbol(order.getSymbol());
                marketOrder.setSide(order.getSign() == 1 ? "BUY" : "SELL");
                marketOrder.setQuantity(order.getRemainingQuantity());
                marketOrder.setPrice(order.getPrice());
                marketOrder.setOrderCount(1);
                marketOrder.setDateOrder(order.getDateOrder() != null ? order.getDateOrder().atStartOfDay() : LocalDateTime.now());
                marketOrder.setOrderMarketId(orderMarketId);
                marketOrder.setOrderType(order.getOrderTypeId());
                marketOrder.setIsOwnOrder(false);
                marketOrder.setDelete(false);
                marketOrder.setDeleteAll(false);
                
                marketOrderbookRepository.save(marketOrder);
                log.info("Created market orderbook entry for order {}", order.getId());
            }
        } catch (Exception e) {
            log.error("Error syncing order {} to market orderbook: {}", order.getId(), e.getMessage(), e);
        }
    }

    /**
     * Remove order from market orderbook
     */
    private void removeOrderFromMarketOrderbook(Order order) {
        try {
            String orderMarketId = "ORDER_" + order.getId();
            marketOrderbookRepository.findByOrderMarketId(orderMarketId)
                .ifPresent(entry -> {
                    entry.setDelete(true);
                    marketOrderbookRepository.save(entry);
                    log.info("Marked market orderbook entry as deleted for order {}", order.getId());
                });
        } catch (Exception e) {
            log.error("Error removing order {} from market orderbook: {}", order.getId(), e.getMessage(), e);
        }
    }

    /**
     * Broadcast orderbook update via WebSocket for a symbol
     */
    private void broadcastOrderbookUpdate(String symbol) {
        try {
            log.info("Preparing to broadcast orderbook update for symbol: {}", symbol);
            
            var buyOrders = marketService.getBuyOrdersBySymbol(symbol);
            var sellOrders = marketService.getSellOrdersBySymbol(symbol);
            
            log.info("Found {} buy orders and {} sell orders for symbol: {}", 
                     buyOrders.size(), sellOrders.size(), symbol);
            
            Map<String, Object> orderbook = Map.of(
                "buy", marketMapper.toOrderbookDtoList(buyOrders),
                "sell", marketMapper.toOrderbookDtoList(sellOrders)
            );
            
            webSocketHandler.broadcastOrderbook(symbol, orderbook);
            log.info("Successfully broadcasted orderbook update for symbol: {}", symbol);
        } catch (Exception e) {
            log.error("Error broadcasting orderbook update for symbol {}: {}", symbol, e.getMessage(), e);
        }
    }
}
