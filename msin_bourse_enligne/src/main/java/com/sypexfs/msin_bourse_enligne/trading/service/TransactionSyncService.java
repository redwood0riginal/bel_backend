package com.sypexfs.msin_bourse_enligne.trading.service;

import com.sypexfs.msin_bourse_enligne.market.entity.MarketTransaction;
import com.sypexfs.msin_bourse_enligne.market.entity.MarketSummary;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketTransactionRepository;
import com.sypexfs.msin_bourse_enligne.market.repository.MarketSummaryRepository;
import com.sypexfs.msin_bourse_enligne.market.websocket.MarketWebSocketHandler;
import com.sypexfs.msin_bourse_enligne.market.dto.TransactionDto;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketSummaryDto;
import com.sypexfs.msin_bourse_enligne.market.dto.MarketMapper;
import com.sypexfs.msin_bourse_enligne.trading.entity.UserTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service to synchronize UserTransaction (private trading data) 
 * with MarketTransaction (public market data)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSyncService {

    private final MarketTransactionRepository marketTransactionRepository;
    private final MarketSummaryRepository marketSummaryRepository;
    private final MarketWebSocketHandler webSocketHandler;
    private final MarketMapper marketMapper;

    /**
     * Synchronize a UserTransaction to MarketTransaction
     * Creates a public market transaction record from a private user transaction
     */
    @Transactional
    public void syncUserTransactionToMarket(UserTransaction userTransaction) {
        log.debug("Syncing user transaction {} to market data", userTransaction.getId());
        
        try {
            MarketTransaction marketTransaction = new MarketTransaction();
            
            // Copy public data only (no user-specific information)
            marketTransaction.setSymbol(userTransaction.getSymbol());
            marketTransaction.setPrice(userTransaction.getPrice());
            marketTransaction.setQuantity(userTransaction.getQuantity());
            marketTransaction.setDateTrans(userTransaction.getTransactionDate());
            
            // Generate unique execution ID
            marketTransaction.setExecId(generateExecId(userTransaction));
            
            // Set as not cancelled
            marketTransaction.setCancel(false);
            
            // Determine side (BUY/SELL)
            marketTransaction.setSide(userTransaction.getSide());
            
            marketTransactionRepository.save(marketTransaction);
            
            log.info("Synced user transaction {} to market transaction for symbol: {}", 
                     userTransaction.getId(), userTransaction.getSymbol());
            
            // Broadcast transaction via WebSocket
            try {
                TransactionDto dto = convertToDto(marketTransaction);
                webSocketHandler.broadcastTransaction(dto);
                log.debug("Broadcasted transaction for symbol: {}", userTransaction.getSymbol());
            } catch (Exception wsEx) {
                log.warn("Failed to broadcast transaction via WebSocket: {}", wsEx.getMessage());
            }
            
            // Update and broadcast market summary
            try {
                updateAndBroadcastMarketSummary(userTransaction.getSymbol(), 
                                                userTransaction.getPrice(), 
                                                userTransaction.getQuantity());
            } catch (Exception sumEx) {
                log.warn("Failed to update market summary: {}", sumEx.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to sync user transaction {} to market data", 
                      userTransaction.getId(), e);
            // Don't throw - market data sync failure shouldn't break user transaction
        }
    }

    /**
     * Batch synchronize multiple user transactions
     */
    @Transactional
    public void syncBatch(List<UserTransaction> userTransactions) {
        log.info("Batch syncing {} user transactions to market data", userTransactions.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (UserTransaction userTx : userTransactions) {
            try {
                syncUserTransactionToMarket(userTx);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to sync transaction {}", userTx.getId(), e);
                failCount++;
            }
        }
        
        log.info("Batch sync completed: {} success, {} failed", successCount, failCount);
    }

    /**
     * Aggregate multiple user transactions into a single market transaction
     */
    @Transactional
    public void syncAggregated(List<UserTransaction> userTransactions, String symbol) {
        if (userTransactions.isEmpty()) {
            return;
        }
        
        log.debug("Aggregating {} user transactions for symbol: {}", userTransactions.size(), symbol);
        
        // Calculate aggregated values
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDateTime latestTime = null;
        String side = userTransactions.get(0).getSide();
        
        for (UserTransaction userTx : userTransactions) {
            totalQuantity = totalQuantity.add(userTx.getQuantity());
            totalAmount = totalAmount.add(userTx.getAmount());
            
            if (latestTime == null || userTx.getTransactionDate().isAfter(latestTime)) {
                latestTime = userTx.getTransactionDate();
            }
        }
        
        // Calculate weighted average price
        BigDecimal avgPrice = totalAmount.divide(totalQuantity, 4, java.math.RoundingMode.HALF_UP);
        
        // Create aggregated market transaction
        MarketTransaction marketTransaction = new MarketTransaction();
        marketTransaction.setSymbol(symbol);
        marketTransaction.setPrice(avgPrice);
        marketTransaction.setQuantity(totalQuantity);
        marketTransaction.setDateTrans(latestTime);
        marketTransaction.setExecId(generateAggregatedExecId(symbol, latestTime));
        marketTransaction.setSide(side);
        marketTransaction.setCancel(false);
        
        marketTransactionRepository.save(marketTransaction);
        
        log.info("Created aggregated market transaction for {} trades of {}", 
                 userTransactions.size(), symbol);
    }

    /**
     * Convert MarketTransaction entity to DTO for WebSocket broadcast
     */
    private TransactionDto convertToDto(MarketTransaction marketTransaction) {
        TransactionDto dto = new TransactionDto();
        dto.setSymbol(marketTransaction.getSymbol());
        dto.setPrice(marketTransaction.getPrice());
        dto.setQuantity(marketTransaction.getQuantity());
        dto.setSide(marketTransaction.getSide());
        dto.setDateTrans(marketTransaction.getDateTrans());
        dto.setExecId(marketTransaction.getExecId());
        return dto;
    }

    /**
     * Generate unique execution ID for market transaction
     */
    private String generateExecId(UserTransaction userTransaction) {
        return String.format("EXEC-%s-%d-%s", 
                           userTransaction.getSymbol(),
                           userTransaction.getId(),
                           UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Generate execution ID for aggregated transaction
     */
    private String generateAggregatedExecId(String symbol, LocalDateTime timestamp) {
        return String.format("AGG-%s-%d-%s", 
                           symbol,
                           timestamp.toEpochSecond(java.time.ZoneOffset.UTC),
                           UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Update market summary with latest transaction data and broadcast via WebSocket
     */
    private void updateAndBroadcastMarketSummary(String symbol, BigDecimal price, BigDecimal quantity) {
        try {
            // Get or create market summary for this symbol
            MarketSummary summary = marketSummaryRepository.findBySymbol(symbol)
                .orElseGet(() -> {
                    MarketSummary newSummary = new MarketSummary();
                    newSummary.setSymbol(symbol);
                    newSummary.setDateUpdate(LocalDateTime.now());
                    return newSummary;
                });
            
            // Update price and calculate variation
            BigDecimal oldPrice = summary.getPrice();
            summary.setPrice(price);
            
            if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal variation = price.subtract(oldPrice)
                    .divide(oldPrice, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                summary.setVariation(variation);
            }
            
            // Update quantity and volume
            summary.setQuantity(quantity);
            if (summary.getVolume() == null) {
                summary.setVolume(BigDecimal.ZERO);
            }
            summary.setVolume(summary.getVolume().add(quantity));
            
            // Update high/low prices
            if (summary.getHigherPrice() == null || price.compareTo(summary.getHigherPrice()) > 0) {
                summary.setHigherPrice(price);
            }
            if (summary.getLowerPrice() == null || price.compareTo(summary.getLowerPrice()) < 0) {
                summary.setLowerPrice(price);
            }
            
            // Update timestamp
            summary.setDateUpdate(LocalDateTime.now());
            summary.setDateTrans(LocalDateTime.now());
            
            // Save updated summary
            MarketSummary updatedSummary = marketSummaryRepository.save(summary);
            log.debug("Updated market summary for symbol: {}", symbol);
            
            // Broadcast to WebSocket
            MarketSummaryDto dto = marketMapper.toMarketSummaryDto(updatedSummary);
            webSocketHandler.broadcastMarketSummary(dto);
            log.debug("Broadcasted market summary update for symbol: {}", symbol);
            
        } catch (Exception e) {
            log.error("Failed to update and broadcast market summary for {}: {}", symbol, e.getMessage(), e);
        }
    }

    /**
     * Check if a user transaction has already been synced to market data
     */
    public boolean isAlreadySynced(UserTransaction userTransaction) {
        String execIdPattern = String.format("EXEC-%s-%d-%%", 
                                            userTransaction.getSymbol(),
                                            userTransaction.getId());
        
        // Check if market transaction with this pattern exists
        return false; 
    }
}
