package com.sypexfs.msin_bourse_enligne.market.simulator;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketInstrument;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketSummary;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketInstrumentRepository;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketSummaryRepository;
import com.sypexfs.msin_bourse_enligne.market.websocket.MarketWebSocketHandler;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderBook;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderBookDepth;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderMatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Orderbook Simulator Thread
 * Randomly adds orders to the market orderbook and broadcasts via WebSocket
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderbookSimulatorThread implements Runnable {

    private final MarketSummaryRepository summaryRepository;
    private final MarketInstrumentRepository instrumentRepository;
    private final OrderMatchingEngine matchingEngine;
    private final MarketWebSocketHandler webSocketHandler;
    
    private volatile boolean running = false;
    private Thread worker;
    private static final long UPDATE_INTERVAL_MS = 10000; // Add order every 10 seconds
    private static final double MAX_PRICE_DEVIATION_PERCENT = 5.0; // Max 5% deviation from current price
    private static final List<String> ALLOWED_SYMBOLS = Arrays.asList("ADH", "AFG", "AFI", "AFM");
    private final Random random = new Random();

    @Override
    public void run() {
        running = true;
        log.info("OrderbookSimulatorThread started");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Get instruments for allowed symbols only
                List<MarketInstrument> instruments = instrumentRepository.findAll().stream()
                    .filter(i -> ALLOWED_SYMBOLS.contains(i.getSymbol()))
                    .collect(Collectors.toList());
                
                if (!instruments.isEmpty()) {
                    // Randomly select an instrument
                    MarketInstrument instrument = instruments.get(random.nextInt(instruments.size()));
                    String symbol = instrument.getSymbol();
                    
                    // Get current price from summary
                    Optional<MarketSummary> summaryOpt = summaryRepository.findLatestBySymbol(symbol);
                    
                    if (summaryOpt.isPresent()) {
                        MarketSummary summary = summaryOpt.get();
                        BigDecimal currentPrice = summary.getPrice() != null 
                            ? summary.getPrice() 
                            : BigDecimal.valueOf(100.0);
                        
                        // Get order book for this symbol
                        OrderBook orderBook = matchingEngine.getOrderBook(symbol);
                        
                        // Order books should be initialized on startup, but check just in case
                        if (orderBook == null) {
                            log.warn("Order book doesn't exist for {}. This shouldn't happen after initialization.", symbol);
                            continue; // Skip this iteration
                        }
                        
                        // Generate random order parameters
                        boolean isBuy = random.nextBoolean();
                        BigDecimal price = generateOrderPrice(currentPrice, isBuy);
                        BigDecimal quantity = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(100, 10001));
                        
                        // Add order to the order book
                        if (isBuy) {
                            orderBook.addBuyOrder(price, quantity);
                            log.debug("Added BUY order for {}: {} @ {}", symbol, quantity, price);
                        } else {
                            orderBook.addSellOrder(price, quantity);
                            log.debug("Added SELL order for {}: {} @ {}", symbol, quantity, price);
                        }
                        
                        // Broadcast orderbook update via WebSocket
                        broadcastOrderbookUpdate(symbol, orderBook);
                    }
                }
                
                // Sleep before next order
                Thread.sleep(UPDATE_INTERVAL_MS);
                
            } catch (InterruptedException e) {
                log.info("OrderbookSimulatorThread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (IllegalStateException e) {
                // EntityManagerFactory is closed - application is shutting down
                if (e.getMessage() != null && e.getMessage().contains("EntityManagerFactory is closed")) {
                    log.info("EntityManagerFactory closed - stopping OrderbookSimulatorThread");
                    running = false;
                    break;
                }
                log.error("Error in OrderbookSimulatorThread: {}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Error in OrderbookSimulatorThread: {}", e.getMessage(), e);
            }
        }
        
        log.info("OrderbookSimulatorThread stopped");
    }

    /**
     * Generate order price based on current price
     * BUY orders: slightly below current price
     * SELL orders: slightly above current price
     */
    private BigDecimal generateOrderPrice(BigDecimal currentPrice, boolean isBuy) {
        BigDecimal orderPrice;
        
        if (isBuy) {
            // BUY orders: 95% to 99% of current price
            orderPrice = currentPrice.multiply(BigDecimal.valueOf(0.95 + random.nextDouble() * 0.04))
                .setScale(4, RoundingMode.HALF_UP);
        } else {
            // SELL orders: 101% to 105% of current price
            orderPrice = currentPrice.multiply(BigDecimal.valueOf(1.01 + random.nextDouble() * 0.04))
                .setScale(4, RoundingMode.HALF_UP);
        }
        
        // Ensure price is positive
        if (orderPrice.compareTo(BigDecimal.ONE) < 0) {
            orderPrice = BigDecimal.ONE;
        }
        
        return orderPrice;
    }

    /**
     * Start the simulator thread
     */
    public synchronized void startSimulator() {
        if (worker == null || !worker.isAlive()) {
            worker = new Thread(this);
            worker.start();
        }
    }

    /**
     * Stop the simulator thread
     */
    public synchronized void stopSimulator() {
        running = false;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            worker = null;
        }
    }

    /**
     * Check if the simulator is running
     */
    public boolean isRunning() {
        return running && worker != null && worker.isAlive();
    }
    
    /**
     * Broadcast orderbook update via WebSocket
     */
    private void broadcastOrderbookUpdate(String symbol, OrderBook orderBook) {
        try {
            // Get order book depth (top 1000 levels)
            OrderBookDepth depth = orderBook.getDepth(1000);
            
            // Create orderbook data structure for WebSocket
            Map<String, Object> orderbookData = new HashMap<>();
            orderbookData.put("symbol", symbol);
            orderbookData.put("bids", depth.getBids());
            orderbookData.put("asks", depth.getAsks());
            orderbookData.put("timestamp", System.currentTimeMillis());
            
            // Broadcast via WebSocket
            webSocketHandler.broadcastOrderbook(symbol, orderbookData);
            
        } catch (Exception e) {
            log.error("Error broadcasting orderbook update for {}: {}", symbol, e.getMessage());
        }
    }
}
