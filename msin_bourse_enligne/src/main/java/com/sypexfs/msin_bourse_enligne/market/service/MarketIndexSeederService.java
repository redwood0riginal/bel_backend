package com.sypexfs.msin_bourse_enligne.market.service;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketIndex;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketIndexSeederService {

    private final MarketIndexRepository indexRepository;

    /**
     * Generate and save intraday MASI data points
     * Creates data points from 9:00 AM to 3:30 PM with specified interval
     */
    @Transactional
    public int seedMasiIntradayData(LocalDateTime date, int intervalMinutes) {
        log.info("Seeding MASI intraday data for date: {}", date);

        List<MarketIndex> dataPoints = generateMasiIntradayData(date, intervalMinutes);
        List<MarketIndex> savedData = indexRepository.saveAll(dataPoints);

        log.info("Successfully seeded {} MASI data points", savedData.size());
        return savedData.size();
    }

    /**
     * Generate MASI intraday data with realistic price movements
     */
    public List<MarketIndex> generateMasiIntradayData(LocalDateTime date, int intervalMinutes) {
        List<MarketIndex> dataPoints = new ArrayList<>();
        
        // Market hours: 9:00 AM to 3:30 PM
        LocalDateTime startTime = date.with(LocalTime.of(9, 0));
        LocalDateTime endTime = date.with(LocalTime.of(15, 30));
        
        // Starting price for MASI
        BigDecimal basePrice = new BigDecimal("13417.33");
        BigDecimal currentPrice = basePrice;
        
        Random random = new Random();
        LocalDateTime currentTime = startTime;
        
        while (!currentTime.isAfter(endTime)) {
            // Create realistic price movements
            double volatility = getVolatilityForTime(currentTime);
            double change = (random.nextDouble() - 0.5) * volatility;
            
            // Apply change
            currentPrice = currentPrice.add(BigDecimal.valueOf(change));
            
            // Mean reversion to keep price within realistic bounds
            BigDecimal deviation = currentPrice.subtract(basePrice);
            BigDecimal meanReversion = deviation.multiply(BigDecimal.valueOf(-0.02));
            currentPrice = currentPrice.add(meanReversion);
            
            // Ensure price stays within ±1% of base
            BigDecimal minPrice = basePrice.multiply(BigDecimal.valueOf(0.99));
            BigDecimal maxPrice = basePrice.multiply(BigDecimal.valueOf(1.01));
            
            if (currentPrice.compareTo(minPrice) < 0) {
                currentPrice = minPrice;
            }
            if (currentPrice.compareTo(maxPrice) > 0) {
                currentPrice = maxPrice;
            }
            
            // Round to 4 decimal places
            currentPrice = currentPrice.setScale(4, RoundingMode.HALF_UP);
            
            // Create MarketIndex entry
            MarketIndex index = new MarketIndex();
            index.setCode("MASI");
            index.setName("Moroccan All Shares Index");
            index.setMarketPlace("CSE");
            index.setIndexType("MAIN");
            index.setPrice(currentPrice);
            index.setDatePrice(currentTime);
            
            dataPoints.add(index);
            
            // Move to next time interval
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }
        
        return dataPoints;
    }

    /**
     * Get volatility factor based on time of day
     * Higher volatility at market open/close, lower during lunch
     */
    private double getVolatilityForTime(LocalDateTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        
        // Morning session (9:00-10:30) - higher volatility
        if (hour == 9 || (hour == 10 && minute < 30)) {
            return 8.0;
        }
        // Mid-morning (10:30-12:00) - moderate volatility
        else if ((hour == 10 && minute >= 30) || hour == 11) {
            return 3.0;
        }
        // Lunch period (12:00-14:00) - lower volatility
        else if (hour == 12 || hour == 13) {
            return 2.0;
        }
        // Afternoon session (14:00-15:30) - increased volatility
        else {
            return 5.0;
        }
    }

    /**
     * Seed multiple days of MASI data
     */
    @Transactional
    public int seedMasiDataForDateRange(LocalDateTime startDate, LocalDateTime endDate, int intervalMinutes) {
        log.info("Seeding MASI data from {} to {}", startDate, endDate);
        
        int totalSeeded = 0;
        LocalDateTime currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // Skip weekends (Saturday = 6, Sunday = 7)
            if (currentDate.getDayOfWeek().getValue() < 6) {
                totalSeeded += seedMasiIntradayData(currentDate, intervalMinutes);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        log.info("Total MASI data points seeded: {}", totalSeeded);
        return totalSeeded;
    }

    /**
     * Seed all major Moroccan indices for a specific date
     */
    @Transactional
    public int seedAllIndicesForDate(LocalDateTime date) {
        log.info("Seeding all indices for date: {}", date);
        
        List<MarketIndex> indices = new ArrayList<>();
        
        // Define all major indices with their typical values
        indices.add(createIndexEntry("MASI", "Moroccan All Shares Index", "MAIN", "13417.33", date));
        indices.add(createIndexEntry("MADEX", "Moroccan Most Active Shares Index", "MAIN", "10915.45", date));
        indices.add(createIndexEntry("MASI20", "MASI 20", "SIZE", "4000.21", date));
        indices.add(createIndexEntry("MASIE", "MASI ESG", "ESG", "72823.20", date));
        indices.add(createIndexEntry("MASID", "MASI Dividend", "MAIN", "64731.74", date));
        indices.add(createIndexEntry("MADXE", "MADEX ESG", "ESG", "75909.54", date));
        indices.add(createIndexEntry("MADXD", "MADEX Dividend", "MAIN", "67475.15", date));
        indices.add(createIndexEntry("MASIR", "MASI Return", "MAIN", "19870.72", date));
        indices.add(createIndexEntry("MADXR", "MADEX Return", "MAIN", "84343.94", date));
        indices.add(createIndexEntry("MASRN", "MASI Return Net", "MAIN", "19837.94", date));
        indices.add(createIndexEntry("MADRN", "MADEX Return Net", "MAIN", "84343.94", date));
        
        // Sector indices
        indices.add(createIndexEntry("BANK", "Banking Sector", "SECTOR", "7955.04", date));
        indices.add(createIndexEntry("ASSUR", "Insurance Sector", "SECTOR", "1031.30", date));
        indices.add(createIndexEntry("IMMOB", "Real Estate Sector", "SECTOR", "3791.49", date));
        indices.add(createIndexEntry("TCOM", "Telecommunications Sector", "SECTOR", "38067.57", date));
        indices.add(createIndexEntry("MINES", "Mining Sector", "SECTOR", "21167.35", date));
        indices.add(createIndexEntry("BOISS", "Beverages Sector", "SECTOR", "31988.48", date));
        indices.add(createIndexEntry("AGRO", "Agribusiness Sector", "SECTOR", "17856.28", date));
        indices.add(createIndexEntry("P&G", "Oil & Gas Sector", "SECTOR", "12726.25", date));
        indices.add(createIndexEntry("PHARM", "Pharmaceutical Sector", "SECTOR", "17517.47", date));
        indices.add(createIndexEntry("B&MC", "Building Materials Sector", "SECTOR", "86345.90", date));
        indices.add(createIndexEntry("DISTR", "Distribution Sector", "SECTOR", "9837.80", date));
        indices.add(createIndexEntry("TRANS", "Transport Sector", "SECTOR", "37679.72", date));
        indices.add(createIndexEntry("L&SI", "Leisure & Services Sector", "SECTOR", "2455.48", date));
        indices.add(createIndexEntry("EEE", "Electrical & Electronics Sector", "SECTOR", "8288.37", date));
        indices.add(createIndexEntry("L&H", "Leisure & Hotels Sector", "SECTOR", "891.45", date));
        indices.add(createIndexEntry("VIGEO", "Vigeo Index", "ESG", "1031.30", date));
        
        List<MarketIndex> saved = indexRepository.saveAll(indices);
        log.info("Seeded {} indices", saved.size());
        
        return saved.size();
    }

    private MarketIndex createIndexEntry(String code, String name, String type, String price, LocalDateTime datePrice) {
        MarketIndex index = new MarketIndex();
        index.setCode(code);
        index.setName(name);
        index.setMarketPlace("CSE");
        index.setIndexType(type);
        index.setPrice(new BigDecimal(price));
        index.setDatePrice(datePrice);
        return index;
    }

    /**
     * Clear all index data (use with caution!)
     */
    @Transactional
    public void clearAllIndexData() {
        log.warn("Clearing all market index data");
        indexRepository.deleteAll();
    }

    /**
     * Clear MASI data only
     */
    @Transactional
    public void clearMasiData() {
        log.warn("Clearing MASI index data");
        List<MarketIndex> masiData = indexRepository.findByCodeOrderByDatePriceDesc("MASI");
        indexRepository.deleteAll(masiData);
    }
}
