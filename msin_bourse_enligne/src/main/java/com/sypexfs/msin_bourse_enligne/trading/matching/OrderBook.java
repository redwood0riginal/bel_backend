package com.sypexfs.msin_bourse_enligne.trading.matching;

import com.sypexfs.msin_bourse_enligne.trading.entity.Order;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * Order book for a single symbol
 * Maintains buy and sell orders sorted by price-time priority
 */
@Slf4j
@Getter
public class OrderBook {
    
    private final String symbol;
    
    // Buy orders: sorted by price DESC (highest first), then by time ASC
    private final TreeMap<PriceTimeKey, Order> buyOrders;
    
    // Sell orders: sorted by price ASC (lowest first), then by time ASC
    private final TreeMap<PriceTimeKey, Order> sellOrders;
    
    // Stop orders waiting to be triggered
    private final List<Order> stopOrders;
    
    // Order ID to PriceTimeKey mapping for quick lookup
    private final Map<Long, PriceTimeKey> orderKeyMap;
    
    public OrderBook(String symbol) {
        this.symbol = symbol;
        
        // Buy orders: higher price has priority
        this.buyOrders = new TreeMap<>((k1, k2) -> {
            // Handle null prices (MARKET orders)
            if (k1.getPrice() == null && k2.getPrice() == null) {
                return k1.getTimestamp().compareTo(k2.getTimestamp());
            }
            if (k1.getPrice() == null) return 1; // null prices go last
            if (k2.getPrice() == null) return -1;
            
            int priceCompare = k2.getPrice().compareTo(k1.getPrice()); // DESC
            if (priceCompare != 0) return priceCompare;
            return k1.getTimestamp().compareTo(k2.getTimestamp()); // ASC
        });
        
        // Sell orders: lower price has priority
        this.sellOrders = new TreeMap<>((k1, k2) -> {
            // Handle null prices (MARKET orders)
            if (k1.getPrice() == null && k2.getPrice() == null) {
                return k1.getTimestamp().compareTo(k2.getTimestamp());
            }
            if (k1.getPrice() == null) return 1; // null prices go last
            if (k2.getPrice() == null) return -1;
            
            int priceCompare = k1.getPrice().compareTo(k2.getPrice()); // ASC
            if (priceCompare != 0) return priceCompare;
            return k1.getTimestamp().compareTo(k2.getTimestamp()); // ASC
        });
        
        this.stopOrders = new ArrayList<>();
        this.orderKeyMap = new HashMap<>();
    }
    
    /**
     * Add order to the book
     */
    public synchronized void addOrder(Order order) {
        PriceTimeKey key = new PriceTimeKey(order.getPrice(), order.getDateEntry(), order.getId());
        
        if (order.isBuyOrder()) {
            buyOrders.put(key, order);
        } else {
            sellOrders.put(key, order);
        }
        
        orderKeyMap.put(order.getId(), key);
        log.debug("Added {} order to book: {} at price {}", 
                  order.isBuyOrder() ? "BUY" : "SELL", order.getId(), order.getPrice());
    }
    
    /**
     * Add stop order to waiting list
     */
    public synchronized void addStopOrder(Order order) {
        stopOrders.add(order);
        log.debug("Added STOP order to waiting list: {}", order.getId());
    }
    
    /**
     * Remove order from the book
     */
    public synchronized void removeOrder(Long orderId) {
        PriceTimeKey key = orderKeyMap.remove(orderId);
        if (key != null) {
            Order order = buyOrders.remove(key);
            if (order == null) {
                order = sellOrders.remove(key);
            }
            if (order != null) {
                log.debug("Removed order from book: {}", orderId);
            }
        }
        
        // Also check stop orders
        stopOrders.removeIf(o -> o.getId().equals(orderId));
    }
    
    /**
     * Get all buy orders sorted by priority
     */
    public synchronized List<Order> getBuyOrders() {
        return new ArrayList<>(buyOrders.values());
    }
    
    /**
     * Get all sell orders sorted by priority
     */
    public synchronized List<Order> getSellOrders() {
        return new ArrayList<>(sellOrders.values());
    }
    
    /**
     * Get best bid (highest buy price)
     */
    public synchronized BigDecimal getBestBid() {
        if (buyOrders.isEmpty()) {
            return null;
        }
        return buyOrders.firstKey().getPrice();
    }
    
    /**
     * Get best ask (lowest sell price)
     */
    public synchronized BigDecimal getBestAsk() {
        if (sellOrders.isEmpty()) {
            return null;
        }
        return sellOrders.firstKey().getPrice();
    }
    
    /**
     * Get bid-ask spread
     */
    public synchronized BigDecimal getSpread() {
        BigDecimal bid = getBestBid();
        BigDecimal ask = getBestAsk();
        
        if (bid == null || ask == null) {
            return null;
        }
        
        return ask.subtract(bid);
    }
    
    /**
     * Get mid price
     */
    public synchronized BigDecimal getMidPrice() {
        BigDecimal bid = getBestBid();
        BigDecimal ask = getBestAsk();
        
        if (bid == null || ask == null) {
            return null;
        }
        
        return bid.add(ask).divide(BigDecimal.valueOf(2), 4, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Get total buy volume at a price level
     */
    public synchronized BigDecimal getBuyVolumeAtPrice(BigDecimal price) {
        return buyOrders.entrySet().stream()
            .filter(e -> e.getKey().getPrice().compareTo(price) == 0)
            .map(e -> e.getValue().getRemainingQuantity())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get total sell volume at a price level
     */
    public synchronized BigDecimal getSellVolumeAtPrice(BigDecimal price) {
        return sellOrders.entrySet().stream()
            .filter(e -> e.getKey().getPrice().compareTo(price) == 0)
            .map(e -> e.getValue().getRemainingQuantity())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get order book depth (top N levels)
     */
    public synchronized OrderBookDepth getDepth(int levels) {
        List<PriceLevel> bids = new ArrayList<>();
        List<PriceLevel> asks = new ArrayList<>();
        
        // Aggregate buy orders by price (skip orders with null prices or zero volume)
        Map<BigDecimal, BigDecimal> bidMap = new TreeMap<>(Comparator.reverseOrder());
        for (Order order : buyOrders.values()) {
            if (order.getPrice() != null && order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
                bidMap.merge(order.getPrice(), order.getRemainingQuantity(), BigDecimal::add);
            }
        }
        bidMap.entrySet().stream()
            .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0) // Filter out zero volumes
            .limit(levels)
            .forEach(e -> bids.add(new PriceLevel(e.getKey(), e.getValue())));
        
        // Aggregate sell orders by price (skip orders with null prices or zero volume)
        Map<BigDecimal, BigDecimal> askMap = new TreeMap<>();
        for (Order order : sellOrders.values()) {
            if (order.getPrice() != null && order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
                askMap.merge(order.getPrice(), order.getRemainingQuantity(), BigDecimal::add);
            }
        }
        askMap.entrySet().stream()
            .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0) // Filter out zero volumes
            .limit(levels)
            .forEach(e -> asks.add(new PriceLevel(e.getKey(), e.getValue())));
        
        return new OrderBookDepth(symbol, bids, asks);
    }
    
    /**
     * Check stop orders and return those that should be triggered
     */
    public synchronized List<Order> checkStopOrders(BigDecimal currentPrice) {
        List<Order> triggered = new ArrayList<>();
        
        Iterator<Order> iterator = stopOrders.iterator();
        while (iterator.hasNext()) {
            Order stopOrder = iterator.next();
            
            boolean shouldTrigger = stopOrder.isBuyOrder()
                ? currentPrice.compareTo(stopOrder.getStopPrice()) >= 0
                : currentPrice.compareTo(stopOrder.getStopPrice()) <= 0;
            
            if (shouldTrigger) {
                triggered.add(stopOrder);
                iterator.remove();
            }
        }
        
        return triggered;
    }
    
    /**
     * Get order book statistics
     */
    public synchronized OrderBookStats getStats() {
        int totalBuyOrders = buyOrders.size();
        int totalSellOrders = sellOrders.size();
        
        BigDecimal totalBuyVolume = buyOrders.values().stream()
            .map(Order::getRemainingQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSellVolume = sellOrders.values().stream()
            .map(Order::getRemainingQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new OrderBookStats(
            symbol,
            totalBuyOrders,
            totalSellOrders,
            totalBuyVolume,
            totalSellVolume,
            getBestBid(),
            getBestAsk(),
            getSpread(),
            getMidPrice()
        );
    }
    
    /**
     * Clear all orders from the book
     */
    public synchronized void clear() {
        buyOrders.clear();
        sellOrders.clear();
        stopOrders.clear();
        orderKeyMap.clear();
        log.info("Cleared order book for symbol: {}", symbol);
    }
    
    /**
     * Add a simulated buy order (for market simulation)
     * Creates a synthetic order without database persistence
     */
    public synchronized void addBuyOrder(BigDecimal price, BigDecimal quantity) {
        PriceTimeKey key = new PriceTimeKey(price, java.time.LocalDateTime.now(), System.nanoTime());
        Order simulatedOrder = createSimulatedOrder(price, quantity, true);
        buyOrders.put(key, simulatedOrder);
        log.debug("Added simulated BUY order: {} @ {}", quantity, price);
    }
    
    /**
     * Add a simulated sell order (for market simulation)
     * Creates a synthetic order without database persistence
     */
    public synchronized void addSellOrder(BigDecimal price, BigDecimal quantity) {
        PriceTimeKey key = new PriceTimeKey(price, java.time.LocalDateTime.now(), System.nanoTime());
        Order simulatedOrder = createSimulatedOrder(price, quantity, false);
        sellOrders.put(key, simulatedOrder);
        log.debug("Added simulated SELL order: {} @ {}", quantity, price);
    }
    
    /**
     * Create a simulated order for market simulation
     */
    private Order createSimulatedOrder(BigDecimal price, BigDecimal quantity, boolean isBuy) {
        Order order = new Order();
        order.setId(System.nanoTime()); // Use nanosecond timestamp as unique ID
        order.setSymbol(this.symbol);
        order.setSign(isBuy ? 1 : -1); // 1 for BUY, -1 for SELL
        order.setOrderTypeId("LIMIT");
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setExecQty(BigDecimal.ZERO); // No executed quantity yet
        order.setDateEntry(java.time.LocalDateTime.now());
        order.setStatId("PENDING");
        order.setUserId(0L); // Simulated order has no user
        return order;
    }
}
