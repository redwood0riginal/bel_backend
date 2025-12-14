package com.sypexfs.msin_bourse_enligne.portfolio.service;

import com.sypexfs.msin_bourse_enligne.portfolio.dto.*;
import com.sypexfs.msin_bourse_enligne.portfolio.entity.Portfolio;
import com.sypexfs.msin_bourse_enligne.portfolio.entity.PortfolioPosition;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {
    
    /**
     * Create a new portfolio for a user
     */
    PortfolioResponse createPortfolio(CreatePortfolioRequest request);
    
    /**
     * Get portfolio by ID
     */
    PortfolioResponse getPortfolio(Long portfolioId);
    
    /**
     * Get user's active portfolio
     */
    PortfolioResponse getUserActivePortfolio(Long userId);
    
    /**
     * Get portfolio with all positions
     */
    PortfolioDetailResponse getPortfolioDetail(Long portfolioId);
    
    /**
     * Get all portfolios for a user
     */
    List<PortfolioResponse> getUserPortfolios(Long userId);
    
    /**
     * Get all positions for a portfolio
     */
    List<PositionResponse> getPortfolioPositions(Long portfolioId);
    
    /**
     * Get a specific position
     */
    PositionResponse getPosition(Long portfolioId, String symbol);
    
    /**
     * Update portfolio cash balance
     */
    PortfolioResponse updateCashBalance(Long portfolioId, BigDecimal amount);
    
    /**
     * Recalculate portfolio summary and positions
     */
    void recalculatePortfolio(Long portfolioId);
    
    /**
     * Update position with current market price
     */
    void updatePositionPrice(Long portfolioId, String symbol, BigDecimal currentPrice);
    
    /**
     * Get portfolio performance metrics
     */
    PerformanceResponse getPortfolioPerformance(Long portfolioId);
    
    /**
     * Deactivate a portfolio
     */
    void deactivatePortfolio(Long portfolioId);
}
