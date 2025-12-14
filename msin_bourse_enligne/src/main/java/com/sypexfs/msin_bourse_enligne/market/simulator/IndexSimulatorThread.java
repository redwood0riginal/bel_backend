package com.sypexfs.msin_bourse_enligne.market.simulator;

import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketIndexSummary;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketIndexSummaryRepository;
import com.sypexfs.msin_bourse_enligne.market.websocket.MarketWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Index Simulator Thread
 * Updates MASI and MASI 20 prices and broadcasts via WebSocket
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IndexSimulatorThread implements Runnable {

    private final MarketIndexSummaryRepository indexSummaryRepository;
    private final MarketWebSocketHandler webSocketHandler;
    private final MarketMapper marketMapper;

    private volatile boolean running = false;
    private Thread worker;
    private static final long UPDATE_INTERVAL_MS = 2000; // Update every 5 seconds
    private static final double MAX_PRICE_CHANGE_PERCENT = 0.5; // Max 0.5% price change for indices (less volatile than stocks)
    private static final List<String> TARGET_INDICES = Arrays.asList("MASI", "MASI.20");
    private final Random random = new Random();

    @Override
    public void run() {
        running = true;
        log.info("IndexSimulatorThread started");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Get index summaries for target indices
                List<MarketIndexSummary> summaries = indexSummaryRepository.findAll().stream()
                    .filter(s -> TARGET_INDICES.contains(s.getSymbol()))
                    .collect(Collectors.toList());
                
                if (!summaries.isEmpty()) {
                    for (MarketIndexSummary summary : summaries) {
                        // Update the price
                        updateIndexPrice(summary);
                        
                        // Save to database
                        MarketIndexSummary updatedSummary = indexSummaryRepository.save(summary);
                        
                        // Broadcast via WebSocket
                        webSocketHandler.broadcastIndexUpdate(
                            marketMapper.toIndexSummaryDto(updatedSummary)
                        );
                        
                        log.debug("Updated index {} : {} (variation: {}%)", 
                            summary.getSymbol(), 
                            summary.getPrice(), 
                            summary.getVariation());
                    }
                }
                
                // Sleep before next update
                Thread.sleep(UPDATE_INTERVAL_MS);
                
            } catch (InterruptedException e) {
                log.info("IndexSimulatorThread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (IllegalStateException e) {
                // EntityManagerFactory is closed - application is shutting down
                if (e.getMessage() != null && e.getMessage().contains("EntityManagerFactory is closed")) {
                    log.info("EntityManagerFactory closed - stopping IndexSimulatorThread");
                    running = false;
                    break;
                }
                log.error("Error in IndexSimulatorThread: {}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Error in IndexSimulatorThread: {}", e.getMessage(), e);
            }
        }
        
        log.info("IndexSimulatorThread stopped");
    }

    /**
     * Update index price with random variation
     */
    private void updateIndexPrice(MarketIndexSummary summary) {
        if (summary.getPrice() == null || summary.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            summary.setPrice(BigDecimal.valueOf(10000.0)); // Default fallback
        }

        BigDecimal currentPrice = summary.getPrice();
        BigDecimal lastClosingPrice = summary.getLastClosingPrice() != null 
            ? summary.getLastClosingPrice() 
            : currentPrice;

        // Generate random price change
        double changePercent = (random.nextDouble() * 2 - 1) * MAX_PRICE_CHANGE_PERCENT;
        BigDecimal priceChange = currentPrice.multiply(BigDecimal.valueOf(changePercent / 100.0));
        BigDecimal newPrice = currentPrice.add(priceChange).setScale(2, RoundingMode.HALF_UP);

        // Update price fields
        summary.setPrice(newPrice);
        summary.setDatePrice(LocalDateTime.now());

        // Update higher/lower price for the day
        if (summary.getHigherPrice() == null || newPrice.compareTo(summary.getHigherPrice()) > 0) {
            summary.setHigherPrice(newPrice);
        }
        if (summary.getLowerPrice() == null || newPrice.compareTo(summary.getLowerPrice()) < 0) {
            summary.setLowerPrice(newPrice);
        }

        // Calculate variation percentage
        if (lastClosingPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variation = newPrice.subtract(lastClosingPrice)
                .divide(lastClosingPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            summary.setVariation(variation);
        }
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
