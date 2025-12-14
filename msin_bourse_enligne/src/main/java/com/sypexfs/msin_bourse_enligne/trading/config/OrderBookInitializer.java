package com.sypexfs.msin_bourse_enligne.trading.config;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketInstrument;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketSummary;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketInstrumentRepository;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketSummaryRepository;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderMatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes order books for all market instruments on application startup
 * This allows the market simulator to work immediately
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderBookInitializer implements ApplicationRunner {

    private final MarketInstrumentRepository instrumentRepository;
    private final OrderMatchingEngine matchingEngine;
    private final MarketSummaryRepository summaryRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing order books and market summaries for all market instruments...");
        
        // Get all instruments
        List<MarketInstrument> instruments = instrumentRepository.findAll();
        
        if (instruments.isEmpty()) {
            log.warn("No market instruments found. Order books not initialized.");
            return;
        }
        
        // Create order book for each instrument using the public method
        int count = 0;
        int summaryCount = 0;
        for (MarketInstrument instrument : instruments) {
            String symbol = instrument.getSymbol();
            matchingEngine.initializeOrderBook(symbol);
            count++;

            // Initialize MarketSummary if missing
            if (summaryRepository.findLatestBySymbol(symbol).isEmpty()) {
                MarketSummary summary = new MarketSummary();
                summary.setSymbol(symbol);
                summary.setName(instrument.getName());
                summary.setPrice(java.math.BigDecimal.valueOf(100.00));
                summary.setClosingPrice(java.math.BigDecimal.valueOf(100.00));
                summary.setOpeningPrice(java.math.BigDecimal.valueOf(100.00));
                summary.setVariation(java.math.BigDecimal.ZERO);
                summary.setVolume(java.math.BigDecimal.ZERO);
                summary.setDateTrans(java.time.LocalDateTime.now());
                summary.setCreatedAt(java.time.LocalDateTime.now());
                
                summaryRepository.save(summary);
                summaryCount++;
                log.info("Created default MarketSummary for symbol: {}", symbol);
            }
        }
        
        log.info("Successfully initialized {} order books and {} market summaries for {} instruments", count, summaryCount, instruments.size());
    }
}
