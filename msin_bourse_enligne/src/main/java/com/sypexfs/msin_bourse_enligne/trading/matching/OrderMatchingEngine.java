package com.sypexfs.msin_bourse_enligne.trading.matching;

import com.sypexfs.msin_bourse_enligne.trading.entity.Order;
import com.sypexfs.msin_bourse_enligne.trading.entity.OrderHistory;
import com.sypexfs.msin_bourse_enligne.trading.entity.UserTransaction;
import com.sypexfs.msin_bourse_enligne.trading.repository.OrderHistoryRepository;
import com.sypexfs.msin_bourse_enligne.trading.repository.OrderRepository;
import com.sypexfs.msin_bourse_enligne.trading.repository.UserTransactionRepository;
import com.sypexfs.msin_bourse_enligne.trading.service.TransactionSyncService;
import com.sypexfs.msin_bourse_enligne.portfolio.service.PortfolioUpdateService;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketOrderbook;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketOrderbookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core order matching engine that matches buy and sell orders
 * Implements price-time priority matching algorithm
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMatchingEngine {

    private final OrderRepository orderRepository;
    private final UserTransactionRepository userTransactionRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final MarketDataService marketDataService;
    private final TransactionSyncService transactionSyncService;
    private final PortfolioUpdateService portfolioUpdateService;
    private final MarketOrderbookRepository marketOrderbookRepository;
    
    // Order books per symbol (symbol -> OrderBook)
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    
    // Commission and tax rates
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.003"); // 0.3%
    private static final BigDecimal TAX_RATE = new BigDecimal("0.001"); // 0.1%
    private static final BigDecimal MIN_COMMISSION = new BigDecimal("10.00");

    /**
     * Process a new order through the matching engine
     */
    @Transactional
    public synchronized MatchingResult processOrder(Order order) {
        log.info("Processing order: {} for symbol: {}", order.getId(), order.getSymbol());
        
        // Validate order
        if (!isValidOrder(order)) {
            rejectOrder(order, "Invalid order parameters");
            return MatchingResult.rejected(order, "Invalid order parameters");
        }
        
        // Get or create order book for symbol
        OrderBook orderBook = orderBooks.computeIfAbsent(
            order.getSymbol(), 
            k -> new OrderBook(order.getSymbol())
        );
        
        // Handle different order types
        MatchingResult result;
        switch (order.getOrderTypeId()) {
            case "MARKET":
                result = processMarketOrder(order, orderBook);
                break;
            case "LIMIT":
                result = processLimitOrder(order, orderBook);
                break;
            case "STOP":
                result = processStopOrder(order, orderBook);
                break;
            case "STOP_LIMIT":
                result = processStopLimitOrder(order, orderBook);
                break;
            default:
                rejectOrder(order, "Unsupported order type: " + order.getOrderTypeId());
                result = MatchingResult.rejected(order, "Unsupported order type");
        }
        
        // Update market data after matching
        if (result.isExecuted() && !result.getExecutions().isEmpty()) {
            updateMarketData(order.getSymbol(), result);
        }
        
        return result;
    }

    /**
     * Process market order - execute immediately at best available price
     */
    private MatchingResult processMarketOrder(Order order, OrderBook orderBook) {
        log.debug("Processing MARKET order: {}", order.getId());
        
        List<OrderExecution> executions = new ArrayList<>();
        BigDecimal remainingQty = order.getQuantity();
        
        // Get opposite side orders
        List<Order> oppositeOrders = order.isBuyOrder() 
            ? orderBook.getSellOrders() 
            : orderBook.getBuyOrders();
        
        if (oppositeOrders.isEmpty()) {
            // No liquidity - reject market order
            rejectOrder(order, "No liquidity available");
            return MatchingResult.rejected(order, "No liquidity available");
        }
        
        // Match against opposite orders
        for (Order oppositeOrder : oppositeOrders) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            BigDecimal matchQty = remainingQty.min(oppositeOrder.getRemainingQuantity());
            BigDecimal matchPrice = oppositeOrder.getPrice();
            
            OrderExecution execution = executeMatch(order, oppositeOrder, matchQty, matchPrice);
            executions.add(execution);
            
            remainingQty = remainingQty.subtract(matchQty);
        }
        
        // Update order status
        boolean isAggressorFilled = remainingQty.compareTo(BigDecimal.ZERO) == 0;
        if (isAggressorFilled) {
            order.setStatId("FILLED");
            order.setExecQty(order.getQuantity());
        } else if (executions.isEmpty()) {
            order.setStatId("REJECTED");
        } else {
            order.setStatId("PARTIAL");
            order.setExecQty(order.getQuantity().subtract(remainingQty));
        }
        
        orderRepository.save(order);
        
        // Note: Filled MARKET orders are never added to order book, so no need to remove them
        
        return new MatchingResult(order, executions);
    }

    /**
     * Process limit order - execute at specified price or better
     */
    private MatchingResult processLimitOrder(Order order, OrderBook orderBook) {
        log.debug("Processing LIMIT order: {} at price: {}", order.getId(), order.getPrice());
        
        List<OrderExecution> executions = new ArrayList<>();
        BigDecimal remainingQty = order.getQuantity();
        
        // Get opposite side orders that match price criteria
        List<Order> oppositeOrders = order.isBuyOrder() 
            ? orderBook.getSellOrders().stream()
                .filter(o -> o.getPrice().compareTo(order.getPrice()) <= 0)
                .collect(Collectors.toList())
            : orderBook.getBuyOrders().stream()
                .filter(o -> o.getPrice().compareTo(order.getPrice()) >= 0)
                .collect(Collectors.toList());
        
        // Match against opposite orders
        for (Order oppositeOrder : oppositeOrders) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            BigDecimal matchQty = remainingQty.min(oppositeOrder.getRemainingQuantity());
            BigDecimal matchPrice = oppositeOrder.getPrice(); // Price improvement for aggressor
            
            OrderExecution execution = executeMatch(order, oppositeOrder, matchQty, matchPrice);
            executions.add(execution);
            
            remainingQty = remainingQty.subtract(matchQty);
        }
        
        // Update order status
        if (remainingQty.compareTo(BigDecimal.ZERO) == 0) {
            order.setStatId("FILLED");
            order.setExecQty(order.getQuantity());
        } else if (executions.isEmpty()) {
            // No match - add to order book
            order.setStatId("PENDING");
            order.setExecQty(BigDecimal.ZERO);
            orderBook.addOrder(order);
        } else {
            // Partial fill - add remaining to order book
            order.setStatId("PARTIAL");
            order.setExecQty(order.getQuantity().subtract(remainingQty));
            orderBook.addOrder(order);
        }
        
        orderRepository.save(order);
        
        return new MatchingResult(order, executions);
    }

    /**
     * Process stop order - converts to market order when stop price reached
     */
    private MatchingResult processStopOrder(Order order, OrderBook orderBook) {
        log.debug("Processing STOP order: {} with stop price: {}", order.getId(), order.getStopPrice());
        
        // Check if stop price is triggered
        BigDecimal currentPrice = marketDataService.getCurrentPrice(order.getSymbol());
        
        boolean triggered = order.isBuyOrder()
            ? currentPrice.compareTo(order.getStopPrice()) >= 0
            : currentPrice.compareTo(order.getStopPrice()) <= 0;
        
        if (triggered) {
            log.info("Stop order {} triggered at price: {}", order.getId(), currentPrice);
            // Convert to market order
            order.setOrderTypeId("MARKET");
            return processMarketOrder(order, orderBook);
        } else {
            // Keep as pending stop order
            order.setStatId("PENDING");
            order.setExecQty(BigDecimal.ZERO);
            orderBook.addStopOrder(order);
            orderRepository.save(order);
            return MatchingResult.pending(order);
        }
    }

    /**
     * Process stop-limit order - converts to limit order when stop price reached
     */
    private MatchingResult processStopLimitOrder(Order order, OrderBook orderBook) {
        log.debug("Processing STOP_LIMIT order: {} with stop: {} and limit: {}", 
                  order.getId(), order.getStopPrice(), order.getPrice());
        
        // Check if stop price is triggered
        BigDecimal currentPrice = marketDataService.getCurrentPrice(order.getSymbol());
        
        boolean triggered = order.isBuyOrder()
            ? currentPrice.compareTo(order.getStopPrice()) >= 0
            : currentPrice.compareTo(order.getStopPrice()) <= 0;
        
        if (triggered) {
            log.info("Stop-limit order {} triggered at price: {}", order.getId(), currentPrice);
            // Convert to limit order
            order.setOrderTypeId("LIMIT");
            return processLimitOrder(order, orderBook);
        } else {
            // Keep as pending stop order
            order.setStatId("PENDING");
            order.setExecQty(BigDecimal.ZERO);
            orderBook.addStopOrder(order);
            orderRepository.save(order);
            return MatchingResult.pending(order);
        }
    }

    /**
     * Execute a match between two orders
     */
    private OrderExecution executeMatch(Order aggressorOrder, Order passiveOrder, 
                                       BigDecimal quantity, BigDecimal price) {
        log.info("Executing match: {} qty at {} between orders {} and {}", 
                 quantity, price, aggressorOrder.getId(), passiveOrder.getId());
        
        // Update passive order
        BigDecimal newExecQty = passiveOrder.getExecQty().add(quantity);
        passiveOrder.setExecQty(newExecQty);
        
        boolean isFilled = passiveOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0;
        if (isFilled) {
            passiveOrder.setStatId("FILLED");
        } else {
            passiveOrder.setStatId("PARTIAL");
        }
        
        // Calculate average execution price
        if (passiveOrder.getExecAvgPrice() == null) {
            passiveOrder.setExecAvgPrice(price);
        } else {
            BigDecimal totalValue = passiveOrder.getExecAvgPrice()
                .multiply(passiveOrder.getExecQty().subtract(quantity))
                .add(price.multiply(quantity));
            passiveOrder.setExecAvgPrice(totalValue.divide(passiveOrder.getExecQty(), 4, RoundingMode.HALF_UP));
        }
        
        orderRepository.save(passiveOrder);
        
        // Update or remove passive order from order book
        if (isFilled) {
            OrderBook orderBook = orderBooks.get(passiveOrder.getSymbol());
            if (orderBook != null) {
                orderBook.removeOrder(passiveOrder.getId());
                log.debug("Removed filled passive order {} from order book", passiveOrder.getId());
            }
            
            // Also remove from market orderbook table
            removeOrderFromMarketOrderbook(passiveOrder);
        } else {
            // Order is partially filled - update market orderbook with remaining quantity
            updateOrderInMarketOrderbook(passiveOrder);
        }
        
        // Create order history for passive order
        createOrderHistory(passiveOrder, newExecQty.subtract(quantity), newExecQty, "Order matched");
        
        // Create transactions for both orders
        UserTransaction aggressorTx = createTransaction(aggressorOrder, quantity, price);
        UserTransaction passiveTx = createTransaction(passiveOrder, quantity, price);
        
        userTransactionRepository.save(aggressorTx);
        userTransactionRepository.save(passiveTx);
        
        // Sync only the aggressor transaction to market data to avoid duplicates
        // (One market transaction represents the match between buyer and seller)
        transactionSyncService.syncUserTransactionToMarket(aggressorTx);
        
        // Update portfolios with new transactions
        updatePortfoliosForTransactions(aggressorTx, passiveTx);
        
        return new OrderExecution(aggressorOrder.getId(), passiveOrder.getId(), 
                                 quantity, price, LocalDateTime.now());
    }

    /**
     * Create transaction record
     */
    private UserTransaction createTransaction(Order order, BigDecimal quantity, BigDecimal price) {
        BigDecimal amount = quantity.multiply(price);
        BigDecimal commission = calculateCommission(amount);
        BigDecimal tax = amount.multiply(TAX_RATE);
        BigDecimal netAmount = order.isBuyOrder() 
            ? amount.add(commission).add(tax)
            : amount.subtract(commission).subtract(tax);
        
        UserTransaction transaction = new UserTransaction();
        transaction.setOrderId(order.getId());
        transaction.setUserId(order.getUserId());
        transaction.setSymbol(order.getSymbol());
        transaction.setSide(order.isBuyOrder() ? "BUY" : "SELL");
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setAmount(amount);
        transaction.setCommission(commission);
        transaction.setTax(tax);
        transaction.setNetAmount(netAmount);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setSettlementDate(LocalDate.now().plusDays(2)); // T+2 settlement
        transaction.setStatus("SETTLED"); // Immediately settled for demo/testing
        
        return transaction;
    }

    /**
     * Calculate commission
     */
    private BigDecimal calculateCommission(BigDecimal amount) {
        BigDecimal commission = amount.multiply(COMMISSION_RATE);
        return commission.max(MIN_COMMISSION);
    }

    /**
     * Create order history entry
     */
    private void createOrderHistory(Order order, BigDecimal previousExecQty, 
                                    BigDecimal newExecQty, String reason) {
        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setPreviousStatus(order.getStatId());
        history.setNewStatus(order.getStatId());
        history.setPreviousExecQty(previousExecQty);
        history.setNewExecQty(newExecQty);
        history.setChangeReason(reason);
        history.setChangedBy(order.getUserId());
        orderHistoryRepository.save(history);
    }

    /**
     * Reject an order
     */
    private void rejectOrder(Order order, String reason) {
        log.warn("Rejecting order {}: {}", order.getId(), reason);
        order.setStatId("REJECTED");
        orderRepository.save(order);
        createOrderHistory(order, BigDecimal.ZERO, BigDecimal.ZERO, reason);
    }

    /**
     * Validate order parameters
     */
    private boolean isValidOrder(Order order) {
        if (order.getQuantity() == null || order.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if ("LIMIT".equals(order.getOrderTypeId()) || "STOP_LIMIT".equals(order.getOrderTypeId())) {
            if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        }
        
        if ("STOP".equals(order.getOrderTypeId()) || "STOP_LIMIT".equals(order.getOrderTypeId())) {
            if (order.getStopPrice() == null || order.getStopPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Update market data after execution
     */
    private void updateMarketData(String symbol, MatchingResult result) {
        if (result.getExecutions().isEmpty()) {
            return;
        }
        
        OrderExecution lastExecution = result.getExecutions().get(result.getExecutions().size() - 1);
        marketDataService.updateLastTrade(symbol, lastExecution.getPrice(), 
                                         lastExecution.getQuantity(), lastExecution.getTimestamp());
    }

    /**
     * Check and trigger stop orders for a symbol
     */
    public void checkStopOrders(String symbol) {
        OrderBook orderBook = orderBooks.get(symbol);
        if (orderBook == null) {
            return;
        }
        
        BigDecimal currentPrice = marketDataService.getCurrentPrice(symbol);
        List<Order> triggeredStopOrders = orderBook.checkStopOrders(currentPrice);
        
        for (Order stopOrder : triggeredStopOrders) {
            log.info("Stop order {} triggered for symbol {}", stopOrder.getId(), symbol);
            processOrder(stopOrder);
        }
    }

    /**
     * Update portfolios for both sides of a transaction
     */
    private void updatePortfoliosForTransactions(UserTransaction aggressorTx, UserTransaction passiveTx) {
        try {
            log.info("Updating portfolio for aggressor transaction: {}", aggressorTx.getId());
            portfolioUpdateService.processTransaction(aggressorTx);
        } catch (Exception e) {
            log.error("Failed to update portfolio for aggressor transaction {}: {}", 
                     aggressorTx.getId(), e.getMessage(), e);
            // Don't fail the trade execution, just log the error
        }
        
        try {
            log.info("Updating portfolio for passive transaction: {}", passiveTx.getId());
            portfolioUpdateService.processTransaction(passiveTx);
        } catch (Exception e) {
            log.error("Failed to update portfolio for passive transaction {}: {}", 
                     passiveTx.getId(), e.getMessage(), e);
            // Don't fail the trade execution, just log the error
        }
    }

    /**
     * Get order book for a symbol
     */
    public OrderBook getOrderBook(String symbol) {
        return orderBooks.get(symbol);
    }

    /**
     * Get all symbols with active order books
     */
    public List<String> getAllSymbols() {
        return new ArrayList<>(orderBooks.keySet());
    }
    
    /**
     * Initialize an empty order book for a symbol
     * Used for market simulation and startup initialization
     */
    public synchronized OrderBook initializeOrderBook(String symbol) {
        return orderBooks.computeIfAbsent(symbol, OrderBook::new);
    }

    /**
     * Update order in market orderbook table with remaining quantity
     */
    private void updateOrderInMarketOrderbook(Order order) {
        try {
            String orderMarketId = "ORDER_" + order.getId();
            marketOrderbookRepository.findByOrderMarketId(orderMarketId)
                .ifPresent(entry -> {
                    entry.setQuantity(order.getRemainingQuantity());
                    entry.setDelete(false);
                    marketOrderbookRepository.save(entry);
                    log.info("Updated market orderbook entry for order {} with remaining qty: {}", 
                             order.getId(), order.getRemainingQuantity());
                });
        } catch (Exception e) {
            log.error("Error updating order {} in market orderbook: {}", order.getId(), e.getMessage(), e);
        }
    }

    /**
     * Remove order from market orderbook table
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
     * Load pending orders from database on startup
     */
    public void loadPendingOrders() {
        log.info("Loading pending orders from database");
        
        // Get all pending and partial orders
        List<Order> pendingOrders = orderRepository.findAll().stream()
            .filter(order -> "PENDING".equals(order.getStatId()) || "PARTIAL".equals(order.getStatId()))
            .collect(Collectors.toList());
        
        log.info("Found {} pending orders to process", pendingOrders.size());
        
        int matched = 0;
        for (Order order : pendingOrders) {
            try {
                // Process each order through the matching engine
                MatchingResult result = processOrder(order);
                if (result.isExecuted() && !result.getExecutions().isEmpty()) {
                    matched++;
                    log.info("Order {} matched during startup with {} executions", 
                             order.getId(), result.getExecutions().size());
                }
            } catch (Exception e) {
                log.error("Error processing order {} during startup: {}", order.getId(), e.getMessage());
                // Continue with next order
            }
        }
        
        log.info("Startup matching completed: {} orders matched", matched);
    }
}
