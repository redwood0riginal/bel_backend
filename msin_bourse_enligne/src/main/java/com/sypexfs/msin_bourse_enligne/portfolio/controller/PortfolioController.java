package com.sypexfs.msin_bourse_enligne.portfolio.controller;

import com.sypexfs.msin_bourse_enligne.portfolio.dto.*;
import com.sypexfs.msin_bourse_enligne.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * Create a new portfolio for authenticated user
     * POST /api/portfolio
     */
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @Valid @RequestBody CreatePortfolioRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        log.info("Creating portfolio for authenticated user: {}", userId);
        request.setUserId(userId); // Override with authenticated user ID
        PortfolioResponse response = portfolioService.createPortfolio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get portfolio by ID
     * GET /api/portfolio/{portfolioId}
     */
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long portfolioId) {
        log.debug("Getting portfolio: {}", portfolioId);
        PortfolioResponse response = portfolioService.getPortfolio(portfolioId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get portfolio with all positions
     * GET /api/portfolio/{portfolioId}/detail
     */
    @GetMapping("/{portfolioId}/detail")
    public ResponseEntity<PortfolioDetailResponse> getPortfolioDetail(@PathVariable Long portfolioId) {
        log.debug("Getting portfolio detail: {}", portfolioId);
        PortfolioDetailResponse response = portfolioService.getPortfolioDetail(portfolioId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user's active portfolio (admin endpoint)
     * GET /api/portfolio/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<PortfolioResponse> getUserActivePortfolio(@PathVariable Long userId) {
        log.debug("Getting active portfolio for user: {}", userId);
        PortfolioResponse response = portfolioService.getUserActivePortfolio(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get authenticated user's active portfolio
     * GET /api/portfolio/my/active
     */
    @GetMapping("/my/active")
    public ResponseEntity<PortfolioResponse> getMyActivePortfolio(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        log.debug("Getting active portfolio for authenticated user: {}", userId);
        PortfolioResponse response = portfolioService.getUserActivePortfolio(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all portfolios for a user (admin endpoint)
     * GET /api/portfolio/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PortfolioResponse>> getUserPortfolios(@PathVariable Long userId) {
        log.debug("Getting all portfolios for user: {}", userId);
        List<PortfolioResponse> response = portfolioService.getUserPortfolios(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all portfolios for authenticated user
     * GET /api/portfolio/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<PortfolioResponse>> getMyPortfolios(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        log.debug("Getting all portfolios for authenticated user: {}", userId);
        List<PortfolioResponse> response = portfolioService.getUserPortfolios(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all positions for a portfolio
     * GET /api/portfolio/{portfolioId}/positions
     */
    @GetMapping("/{portfolioId}/positions")
    public ResponseEntity<List<PositionResponse>> getPortfolioPositions(@PathVariable Long portfolioId) {
        log.debug("Getting positions for portfolio: {}", portfolioId);
        List<PositionResponse> response = portfolioService.getPortfolioPositions(portfolioId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific position
     * GET /api/portfolio/{portfolioId}/positions/{symbol}
     */
    @GetMapping("/{portfolioId}/positions/{symbol}")
    public ResponseEntity<PositionResponse> getPosition(
            @PathVariable Long portfolioId,
            @PathVariable String symbol) {
        log.debug("Getting position {} for portfolio: {}", symbol, portfolioId);
        PositionResponse response = portfolioService.getPosition(portfolioId, symbol);
        return ResponseEntity.ok(response);
    }

    /**
     * Update cash balance
     * PUT /api/portfolio/{portfolioId}/cash
     */
    @PutMapping("/{portfolioId}/cash")
    public ResponseEntity<PortfolioResponse> updateCashBalance(
            @PathVariable Long portfolioId,
            @RequestBody Map<String, BigDecimal> request) {
        log.info("Updating cash balance for portfolio: {}", portfolioId);
        BigDecimal amount = request.get("amount");
        PortfolioResponse response = portfolioService.updateCashBalance(portfolioId, amount);
        return ResponseEntity.ok(response);
    }

    /**
     * Recalculate portfolio
     * POST /api/portfolio/{portfolioId}/recalculate
     */
    @PostMapping("/{portfolioId}/recalculate")
    public ResponseEntity<Void> recalculatePortfolio(@PathVariable Long portfolioId) {
        log.info("Recalculating portfolio: {}", portfolioId);
        portfolioService.recalculatePortfolio(portfolioId);
        return ResponseEntity.ok().build();
    }

    /**
     * Update position price
     * PUT /api/portfolio/{portfolioId}/positions/{symbol}/price
     */
    @PutMapping("/{portfolioId}/positions/{symbol}/price")
    public ResponseEntity<Void> updatePositionPrice(
            @PathVariable Long portfolioId,
            @PathVariable String symbol,
            @RequestBody Map<String, BigDecimal> request) {
        log.debug("Updating price for position {} in portfolio: {}", symbol, portfolioId);
        BigDecimal currentPrice = request.get("currentPrice");
        portfolioService.updatePositionPrice(portfolioId, symbol, currentPrice);
        return ResponseEntity.ok().build();
    }

    /**
     * Get portfolio performance
     * GET /api/portfolio/{portfolioId}/performance
     */
    @GetMapping("/{portfolioId}/performance")
    public ResponseEntity<PerformanceResponse> getPortfolioPerformance(@PathVariable Long portfolioId) {
        log.debug("Getting performance for portfolio: {}", portfolioId);
        PerformanceResponse response = portfolioService.getPortfolioPerformance(portfolioId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate portfolio
     * DELETE /api/portfolio/{portfolioId}
     */
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deactivatePortfolio(@PathVariable Long portfolioId) {
        log.info("Deactivating portfolio: {}", portfolioId);
        portfolioService.deactivatePortfolio(portfolioId);
        return ResponseEntity.ok().build();
    }

    /**
     * Health check endpoint
     * GET /api/portfolio/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "portfolio"));
    }

    // ==================== Helper Methods ====================

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.sypexfs.msin_bourse_enligne.auth.security.UserDetailsImpl) {
            com.sypexfs.msin_bourse_enligne.auth.security.UserDetailsImpl userDetails = 
                (com.sypexfs.msin_bourse_enligne.auth.security.UserDetailsImpl) principal;
            return userDetails.getId();
        }
        
        throw new RuntimeException("Invalid authentication principal type");
    }
}
