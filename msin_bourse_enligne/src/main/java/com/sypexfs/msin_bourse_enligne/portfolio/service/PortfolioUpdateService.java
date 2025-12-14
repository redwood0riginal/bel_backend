package com.sypexfs.msin_bourse_enligne.portfolio.service;

import com.sypexfs.msin_bourse_enligne.portfolio.entity.Portfolio;
import com.sypexfs.msin_bourse_enligne.portfolio.entity.PortfolioPosition;
import com.sypexfs.msin_bourse_enligne.portfolio.entity.PortfolioSummary;
import com.sypexfs.msin_bourse_enligne.portfolio.repository.PortfolioPositionRepository;
import com.sypexfs.msin_bourse_enligne.portfolio.repository.PortfolioRepository;
import com.sypexfs.msin_bourse_enligne.portfolio.repository.PortfolioSummaryRepository;
import com.sypexfs.msin_bourse_enligne.trading.entity.UserTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for updating portfolio positions based on executed transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioUpdateService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioPositionRepository positionRepository;
    private final PortfolioSummaryRepository summaryRepository;
    private final PortfolioService portfolioService;

    /**
     * Process a transaction and update portfolio accordingly
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void processTransaction(UserTransaction transaction) {
        log.info("Processing transaction for portfolio update: {}", transaction.getId());
        
        // Get user's active portfolio
        Portfolio portfolio = portfolioRepository.findActivePortfolioByUserId(transaction.getUserId())
                .orElseThrow(() -> new com.sypexfs.msin_bourse_enligne.common.exception.ResourceNotFoundException(
                    "Active portfolio", "userId", transaction.getUserId()));
        
        if ("BUY".equals(transaction.getSide())) {
            processBuyTransaction(portfolio, transaction);
        } else if ("SELL".equals(transaction.getSide())) {
            processSellTransaction(portfolio, transaction);
        }
        
        // Recalculate portfolio after transaction
        portfolioService.recalculatePortfolio(portfolio.getId());
        
        log.info("Portfolio updated for transaction: {}", transaction.getId());
    }

    private void processBuyTransaction(Portfolio portfolio, UserTransaction transaction) {
        PortfolioPosition position = positionRepository
                .findByPortfolioIdAndSymbol(portfolio.getId(), transaction.getSymbol())
                .orElse(null);
        
        if (position == null) {
            // Create new position
            position = new PortfolioPosition();
            position.setPortfolio(portfolio);
            position.setSymbol(transaction.getSymbol());
            position.setQuantity(transaction.getQuantity());
            position.setAverageCost(transaction.getPrice());
            position.setCurrentPrice(transaction.getPrice());
            position.setLastUpdated(LocalDateTime.now());
            
            log.info("Created new position: {} with quantity: {}", transaction.getSymbol(), transaction.getQuantity());
        } else {
            // Update existing position - calculate new average cost
            BigDecimal currentValue = position.getQuantity().multiply(position.getAverageCost());
            BigDecimal newValue = transaction.getQuantity().multiply(transaction.getPrice());
            BigDecimal totalValue = currentValue.add(newValue);
            BigDecimal totalQuantity = position.getQuantity().add(transaction.getQuantity());
            
            BigDecimal newAverageCost = totalValue.divide(totalQuantity, 4, RoundingMode.HALF_UP);
            
            position.setQuantity(totalQuantity);
            position.setAverageCost(newAverageCost);
            position.setCurrentPrice(transaction.getPrice());
            position.setLastUpdated(LocalDateTime.now());
            
            log.info("Updated position: {} - new quantity: {}, new avg cost: {}", 
                    transaction.getSymbol(), totalQuantity, newAverageCost);
        }
        
        positionRepository.save(position);
        
        // Update cash balance
        PortfolioSummary summary = summaryRepository.findByPortfolioId(portfolio.getId())
                .orElseThrow(() -> new RuntimeException("Portfolio summary not found"));
        
        BigDecimal totalCost = transaction.getNetAmount(); // includes commission and tax
        summary.setCashBalance(summary.getCashBalance().subtract(totalCost));
        summaryRepository.save(summary);
    }

    private void processSellTransaction(Portfolio portfolio, UserTransaction transaction) {
        PortfolioPosition position = positionRepository
                .findByPortfolioIdAndSymbol(portfolio.getId(), transaction.getSymbol())
                .orElseThrow(() -> new RuntimeException("Position not found for sell: " + transaction.getSymbol()));
        
        BigDecimal newQuantity = position.getQuantity().subtract(transaction.getQuantity());
        
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient quantity to sell");
        }
        
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // Close position
            positionRepository.delete(position);
            log.info("Closed position: {}", transaction.getSymbol());
        } else {
            // Reduce quantity but keep average cost
            position.setQuantity(newQuantity);
            position.setCurrentPrice(transaction.getPrice());
            position.setLastUpdated(LocalDateTime.now());
            positionRepository.save(position);
            
            log.info("Reduced position: {} - new quantity: {}", transaction.getSymbol(), newQuantity);
        }
        
        // Update cash balance
        PortfolioSummary summary = summaryRepository.findByPortfolioId(portfolio.getId())
                .orElseThrow(() -> new RuntimeException("Portfolio summary not found"));
        
        BigDecimal proceeds = transaction.getNetAmount(); // net after commission and tax
        summary.setCashBalance(summary.getCashBalance().add(proceeds));
        summaryRepository.save(summary);
    }

    /**
     * Update all positions with current market prices
     */
    @Transactional
    public void updateAllPositionPrices(Long portfolioId, java.util.Map<String, BigDecimal> priceMap) {
        List<PortfolioPosition> positions = positionRepository.findByPortfolioId(portfolioId);
        
        for (PortfolioPosition position : positions) {
            BigDecimal currentPrice = priceMap.get(position.getSymbol());
            if (currentPrice != null) {
                position.setCurrentPrice(currentPrice);
                position.setLastUpdated(LocalDateTime.now());
            }
        }
        
        positionRepository.saveAll(positions);
        portfolioService.recalculatePortfolio(portfolioId);
        
        log.info("Updated {} positions with current prices", positions.size());
    }
}
