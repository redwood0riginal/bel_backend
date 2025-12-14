package com.sypexfs.msin_bourse_enligne.market.simulator;

import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketSummary;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketSummaryRepository;
import com.sypexfs.msin_bourse_enligne.market.websocket.MarketWebSocketHandler;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketOverviewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Market Price Simulator Thread
 * Randomly updates stock prices and broadcasts via WebSocket
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MarketPriceSimulatorThread implements Runnable {

    private final MarketSummaryRepository summaryRepository;
    private final MarketWebSocketHandler webSocketHandler;
    private final MarketMapper marketMapper;
    private final MarketService marketService;
    
    private volatile boolean running = false;
    private Thread worker;
    private static final long UPDATE_INTERVAL_MS = 2000; // Update every 5 seconds
    private static final double MAX_PRICE_CHANGE_PERCENT = 2.0; // Max 2% price change
    private static final List<String> ALLOWED_SYMBOLS = Arrays.asList("ADH", "AFG", "AFI", "AFM");
    private final Random random = new Random();

    @Override
    public void run() {
        running = true;
        log.info("MarketPriceSimulatorThread started");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Get market summaries for allowed symbols only
                List<MarketSummary> summaries = summaryRepository.findAll().stream()
                    .filter(s -> ALLOWED_SYMBOLS.contains(s.getSymbol()))
                    .collect(Collectors.toList());
                
                if (!summaries.isEmpty()) {
                    // Randomly select a summary to update
                    MarketSummary summary = summaries.get(random.nextInt(summaries.size()));
                    
                    // Update the price
                    updateMarketPrice(summary);
                    
                    // Save to database
                    MarketSummary updatedSummary = summaryRepository.save(summary);
                    
                    // Broadcast via WebSocket
                    webSocketHandler.broadcastMarketSummary(
                        marketMapper.toMarketSummaryDto(updatedSummary)
                    );
                    
                    log.debug("Updated price for {}: {} (variation: {}%)", 
                        summary.getSymbol(), 
                        summary.getPrice(), 
                        summary.getVariation());
                    
                    // Broadcast market overview
                    MarketOverviewDto overview = MarketOverviewDto.builder()
                        .indices(marketMapper.toIndexSummaryDtoList(marketService.getIndexOverview()))
                        .topGainers(marketMapper.toMarketSummaryDtoList(marketService.getTopGainers(10)))
                        .topLosers(marketMapper.toMarketSummaryDtoList(marketService.getTopLosers(10)))
                        .mostActive(marketMapper.toMarketSummaryDtoList(marketService.getMostActive(10)))
                        .build();
                    webSocketHandler.broadcastMarketOverview(overview);
                }
                
                // Sleep before next update
                Thread.sleep(UPDATE_INTERVAL_MS);
                
            } catch (InterruptedException e) {
                log.info("MarketPriceSimulatorThread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (IllegalStateException e) {
                // EntityManagerFactory is closed - application is shutting down
                if (e.getMessage() != null && e.getMessage().contains("EntityManagerFactory is closed")) {
                    log.info("EntityManagerFactory closed - stopping MarketPriceSimulatorThread");
                    running = false;
                    break;
                }
                log.error("Error in MarketPriceSimulatorThread: {}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Error in MarketPriceSimulatorThread: {}", e.getMessage(), e);
            }
        }
        
        log.info("MarketPriceSimulatorThread stopped");
    }

    /**
     * Update market price with random variation
     */
    private void updateMarketPrice(MarketSummary summary) {
        if (summary.getPrice() == null || summary.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            // Initialize price if null or zero
            summary.setPrice(BigDecimal.valueOf(100.0));
            summary.setLastClosingPrice(BigDecimal.valueOf(100.0));
        }

        BigDecimal currentPrice = summary.getPrice();
        BigDecimal lastClosingPrice = summary.getLastClosingPrice() != null 
            ? summary.getLastClosingPrice() 
            : currentPrice;

        // Generate random price change (-MAX_PRICE_CHANGE_PERCENT to +MAX_PRICE_CHANGE_PERCENT)
        double changePercent = (random.nextDouble() * 2 - 1) * MAX_PRICE_CHANGE_PERCENT;
        BigDecimal priceChange = currentPrice.multiply(BigDecimal.valueOf(changePercent / 100.0));
        BigDecimal newPrice = currentPrice.add(priceChange).setScale(4, RoundingMode.HALF_UP);

        // Ensure price doesn't go below 1
        if (newPrice.compareTo(BigDecimal.ONE) < 0) {
            newPrice = BigDecimal.ONE;
        }

        // Update price fields
        summary.setPrice(newPrice);
        summary.setDateTrans(LocalDateTime.now());
        summary.setDateUpdate(LocalDateTime.now());

        // Update higher/lower price for the day
        if (summary.getHigherPrice() == null || newPrice.compareTo(summary.getHigherPrice()) > 0) {
            summary.setHigherPrice(newPrice);
        }
        if (summary.getLowerPrice() == null || newPrice.compareTo(summary.getLowerPrice()) < 0) {
            summary.setLowerPrice(newPrice);
        }

        // Calculate variation percentage
        BigDecimal variation = newPrice.subtract(lastClosingPrice)
            .divide(lastClosingPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        summary.setVariation(variation);

        // Update volume with random increment
        BigDecimal volumeIncrement = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(100, 1000));
        BigDecimal currentVolume = summary.getVolume() != null ? summary.getVolume() : BigDecimal.ZERO;
        summary.setVolume(currentVolume.add(volumeIncrement));

        // Update quantity
        BigDecimal quantityIncrement = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(10, 100));
        BigDecimal currentQuantity = summary.getQuantity() != null ? summary.getQuantity() : BigDecimal.ZERO;
        summary.setQuantity(currentQuantity.add(quantityIncrement));

        // Calculate VWAP (simplified)
        if (summary.getVolume().compareTo(BigDecimal.ZERO) > 0) {
            summary.setVwap(newPrice.multiply(BigDecimal.valueOf(0.98 + random.nextDouble() * 0.04))
                .setScale(4, RoundingMode.HALF_UP));
        }

        // Update opening price if not set
        if (summary.getOpeningPrice() == null) {
            summary.setOpeningPrice(lastClosingPrice);
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
