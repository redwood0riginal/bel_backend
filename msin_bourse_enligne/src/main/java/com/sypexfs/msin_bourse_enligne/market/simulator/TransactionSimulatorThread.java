package com.sypexfs.msin_bourse_enligne.market.simulator;

import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketInstrument;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketSummary;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketTransaction;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketInstrumentRepository;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketSummaryRepository;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketTransactionRepository;
import com.sypexfs.msin_bourse_enligne.market.websocket.MarketWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Transaction Simulator Thread
 * Randomly generates market transactions and broadcasts via WebSocket
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionSimulatorThread implements Runnable {

    private final MarketTransactionRepository transactionRepository;
    private final MarketSummaryRepository summaryRepository;
    private final MarketInstrumentRepository instrumentRepository;
    private final MarketWebSocketHandler webSocketHandler;
    private final MarketMapper marketMapper;
    
    private volatile boolean running = false;
    private Thread worker;
    private static final long UPDATE_INTERVAL_MS = 10000; // Create transaction every 10 seconds
    private static final double MAX_PRICE_DEVIATION_PERCENT = 1.0; // Max 1% deviation from current price
    private static final List<String> ALLOWED_SYMBOLS = Arrays.asList("ADH", "AFG", "AFI", "AFM");
    private final Random random = new Random();

    @Override
    public void run() {
        running = true;
        log.info("TransactionSimulatorThread started");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Get instruments for allowed symbols only
                List<MarketInstrument> instruments = instrumentRepository.findAll().stream()
                    .filter(i -> ALLOWED_SYMBOLS.contains(i.getSymbol()))
                    .collect(Collectors.toList());
                
                log.debug("Found {} instruments matching allowed symbols: {}", 
                    instruments.size(), 
                    instruments.stream().map(MarketInstrument::getSymbol).collect(Collectors.joining(", ")));
                
                if (!instruments.isEmpty()) {
                    // Randomly select an instrument
                    MarketInstrument instrument = instruments.get(random.nextInt(instruments.size()));
                    String symbol = instrument.getSymbol();
                    log.debug("Selected symbol for transaction: {}", symbol);
                    
                    // Get current price from summary
                    Optional<MarketSummary> summaryOpt = summaryRepository.findLatestBySymbol(symbol);
                    
                    if (summaryOpt.isPresent()) {
                        MarketSummary summary = summaryOpt.get();
                        BigDecimal currentPrice = summary.getPrice() != null 
                            ? summary.getPrice() 
                            : BigDecimal.valueOf(100.0);
                        
                        // Create a random transaction
                        MarketTransaction transaction = createRandomTransaction(symbol, currentPrice, instrument);
                        
                        // Save to database
                        MarketTransaction savedTransaction = transactionRepository.save(transaction);
                        
                        // Broadcast transaction via WebSocket
                        webSocketHandler.broadcastTransaction(
                            marketMapper.toTransactionDto(savedTransaction)
                        );
                        
                        log.debug("Created {} transaction for {}: {} @ {}", 
                            savedTransaction.getSide(), 
                            symbol, 
                            savedTransaction.getQuantity(), 
                            savedTransaction.getPrice());
                    } else {
                        log.warn("No market summary found for symbol: {}", symbol);
                    }
                }
                
                // Sleep before next transaction
                Thread.sleep(UPDATE_INTERVAL_MS);
                
            } catch (InterruptedException e) {
                log.info("TransactionSimulatorThread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (IllegalStateException e) {
                // EntityManagerFactory is closed - application is shutting down
                if (e.getMessage() != null && e.getMessage().contains("EntityManagerFactory is closed")) {
                    log.info("EntityManagerFactory closed - stopping TransactionSimulatorThread");
                    running = false;
                    break;
                }
                log.error("Error in TransactionSimulatorThread: {}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Error in TransactionSimulatorThread: {}", e.getMessage(), e);
            }
        }
        
        log.info("TransactionSimulatorThread stopped");
    }

    /**
     * Create a random transaction
     */
    private MarketTransaction createRandomTransaction(String symbol, BigDecimal currentPrice, MarketInstrument instrument) {
        MarketTransaction transaction = new MarketTransaction();
        
        // Set symbol and identifiers
        transaction.setSymbol(symbol);
        transaction.setSecId(instrument.getSymbol());
        transaction.setMarketPlace(instrument.getMarketPlace() != null ? instrument.getMarketPlace() : "CASABLANCA");
        
        // Random side (BUY or SELL)
        String side = random.nextBoolean() ? "BUY" : "SELL";
        transaction.setSide(side);
        
        // Generate execution price close to current price (within 1% deviation)
        double deviationPercent = (random.nextDouble() * 2 - 1) * MAX_PRICE_DEVIATION_PERCENT;
        BigDecimal priceDeviation = currentPrice.multiply(BigDecimal.valueOf(deviationPercent / 100.0));
        BigDecimal executionPrice = currentPrice.add(priceDeviation).setScale(4, RoundingMode.HALF_UP);
        
        // Ensure price is positive
        if (executionPrice.compareTo(BigDecimal.ONE) < 0) {
            executionPrice = BigDecimal.ONE;
        }
        
        transaction.setPrice(executionPrice);
        
        // Random quantity (10 to 5000)
        BigDecimal quantity = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(10, 5001));
        transaction.setQuantity(quantity);
        
        // Set transaction timestamp
        transaction.setDateTrans(LocalDateTime.now());
        
        // Generate unique execution ID
        transaction.setExecId(generateExecId(symbol));
        
        // Set execution type
        String[] execTypes = {"TRADE", "FILL", "PARTIAL_FILL", "MATCH"};
        transaction.setExecType(execTypes[random.nextInt(execTypes.length)]);
        
        // Set trade type
        String[] tradeTypes = {"REGULAR", "BLOCK", "AUCTION", "OTC"};
        transaction.setTradeType(tradeTypes[random.nextInt(tradeTypes.length)]);
        
        // Generate sequence number
        transaction.setSequence(String.valueOf(System.currentTimeMillis() % 1000000));
        
        // Generate order ID
        transaction.setOrderId(generateOrderId(symbol));
        
        // Set flags
        transaction.setCancel(false);
        
        // Set nanosecond precision (optional)
        transaction.setNanoSecond(BigDecimal.valueOf(System.nanoTime() % 1000000000));
        
        return transaction;
    }

    /**
     * Generate unique execution ID
     */
    private String generateExecId(String symbol) {
        return String.format("EXEC-%s-%d-%d", 
            symbol, 
            System.currentTimeMillis(), 
            ThreadLocalRandom.current().nextInt(10000, 99999));
    }

    /**
     * Generate order ID
     */
    private String generateOrderId(String symbol) {
        return String.format("ORD-%s-%d", 
            symbol, 
            ThreadLocalRandom.current().nextLong(100000, 999999));
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

    public boolean isRunning() {
        return running && worker != null && worker.isAlive();
    }
}
