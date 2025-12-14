package com.sypexfs.msin_bourse_enligne.portfolio.service;

import com.sypexfs.msin_bourse_enligne.portfolio.dto.*;
import com.sypexfs.msin_bourse_enligne.portfolio.entity.Portfolio;
import com.sypexfs.msin_bourse_enligne.portfolio.entity.PortfolioPosition;
import com.sypexfs.msin_bourse_enligne.portfolio.entity.PortfolioSummary;
import com.sypexfs.msin_bourse_enligne.portfolio.repository.PortfolioPositionRepository;
import com.sypexfs.msin_bourse_enligne.portfolio.repository.PortfolioRepository;
import com.sypexfs.msin_bourse_enligne.portfolio.repository.PortfolioSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioPositionRepository positionRepository;
    private final PortfolioSummaryRepository summaryRepository;

    @Override
    @Transactional
    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        log.info("Creating portfolio for user: {}", request.getUserId());
        
        // Generate unique account number
        String accountNumber = generateAccountNumber();
        
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(request.getUserId());
        portfolio.setAccountNumber(accountNumber);
        portfolio.setAccountType(request.getAccountType());
        portfolio.setCurrency(request.getCurrency());
        portfolio.setStatus("ACTIVE");
        
        portfolio = portfolioRepository.save(portfolio);
        
        // Create initial summary
        PortfolioSummary summary = new PortfolioSummary();
        summary.setPortfolio(portfolio);
        summary.setCashBalance(request.getInitialCashBalance());
        summary.setSecuritiesValue(BigDecimal.ZERO);
        summary.setTotalValue(request.getInitialCashBalance());
        summary.setInvestedAmount(BigDecimal.ZERO);
        summary.setTotalPnl(BigDecimal.ZERO);
        summary.setTotalPnlPercent(BigDecimal.ZERO);
        summary.setDayPnl(BigDecimal.ZERO);
        summary.setDayPnlPercent(BigDecimal.ZERO);
        
        summaryRepository.save(summary);
        
        log.info("Portfolio created with ID: {} for user: {}", portfolio.getId(), request.getUserId());
        return mapToResponse(portfolio, summary);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found: " + portfolioId));
        
        PortfolioSummary summary = summaryRepository.findByPortfolioId(portfolioId)
                .orElse(null);
        
        return mapToResponse(portfolio, summary);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getUserActivePortfolio(Long userId) {
        Portfolio portfolio = portfolioRepository.findActivePortfolioByUserId(userId)
                .orElseThrow(() -> new com.sypexfs.msin_bourse_enligne.common.exception.ResourceNotFoundException(
                    "Active portfolio", "userId", userId));
        
        PortfolioSummary summary = summaryRepository.findByPortfolioId(portfolio.getId())
                .orElse(null);
        
        return mapToResponse(portfolio, summary);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioDetailResponse getPortfolioDetail(Long portfolioId) {
        PortfolioResponse portfolio = getPortfolio(portfolioId);
        List<PositionResponse> positions = getPortfolioPositions(portfolioId);
        
        return PortfolioDetailResponse.builder()
                .portfolio(portfolio)
                .positions(positions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getUserPortfolios(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        
        return portfolios.stream()
                .map(portfolio -> {
                    PortfolioSummary summary = summaryRepository.findByPortfolioId(portfolio.getId())
                            .orElse(null);
                    return mapToResponse(portfolio, summary);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> getPortfolioPositions(Long portfolioId) {
        List<PortfolioPosition> positions = positionRepository.findByPortfolioIdOrderByMarketValueDesc(portfolioId);
        
        return positions.stream()
                .map(this::mapToPositionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponse getPosition(Long portfolioId, String symbol) {
        PortfolioPosition position = positionRepository.findByPortfolioIdAndSymbol(portfolioId, symbol)
                .orElseThrow(() -> new RuntimeException("Position not found: " + symbol));
        
        return mapToPositionResponse(position);
    }

    @Override
    @Transactional
    public PortfolioResponse updateCashBalance(Long portfolioId, BigDecimal amount) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found: " + portfolioId));
        
        PortfolioSummary summary = summaryRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio summary not found"));
        
        BigDecimal newCashBalance = summary.getCashBalance().add(amount);
        if (newCashBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient cash balance");
        }
        
        summary.setCashBalance(newCashBalance);
        summaryRepository.save(summary);
        
        recalculatePortfolio(portfolioId);
        
        return mapToResponse(portfolio, summary);
    }

    @Override
    @Transactional
    public void recalculatePortfolio(Long portfolioId) {
        log.debug("Recalculating portfolio: {}", portfolioId);
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found: " + portfolioId));
        
        PortfolioSummary summary = summaryRepository.findByPortfolioId(portfolioId)
                .orElseGet(() -> {
                    PortfolioSummary newSummary = new PortfolioSummary();
                    newSummary.setPortfolio(portfolio);
                    newSummary.setCashBalance(BigDecimal.ZERO);
                    return newSummary;
                });
        
        List<PortfolioPosition> positions = positionRepository.findByPortfolioId(portfolioId);
        
        // Calculate total market value of positions
        BigDecimal totalPositionValue = positions.stream()
                .map(PortfolioPosition::getMarketValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total invested amount
        BigDecimal totalInvested = positions.stream()
                .filter(pos -> pos.getQuantity() != null && pos.getAverageCost() != null)
                .map(pos -> pos.getQuantity().multiply(pos.getAverageCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total unrealized P&L
        BigDecimal totalUnrealizedPnl = positions.stream()
                .map(PortfolioPosition::getUnrealizedPnl)
                .filter(pnl -> pnl != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Update summary
        summary.setSecuritiesValue(totalPositionValue);
        summary.setInvestedAmount(totalInvested);
        summary.setTotalValue(summary.getCashBalance().add(totalPositionValue));
        summary.setTotalPnl(totalUnrealizedPnl);
        
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            summary.setTotalPnlPercent(
                    totalUnrealizedPnl.divide(totalInvested, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
            );
        } else {
            summary.setTotalPnlPercent(BigDecimal.ZERO);
        }
        
        // Calculate weight percentage for each position
        if (totalPositionValue.compareTo(BigDecimal.ZERO) > 0) {
            for (PortfolioPosition position : positions) {
                if (position.getMarketValue() != null) {
                    BigDecimal weight = position.getMarketValue()
                            .divide(totalPositionValue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    position.setWeightPercent(weight);
                    positionRepository.save(position);
                }
            }
        }
        
        summaryRepository.save(summary);
        log.debug("Portfolio recalculated: {}", portfolioId);
    }

    @Override
    @Transactional
    public void updatePositionPrice(Long portfolioId, String symbol, BigDecimal currentPrice) {
        PortfolioPosition position = positionRepository.findByPortfolioIdAndSymbol(portfolioId, symbol)
                .orElseThrow(() -> new RuntimeException("Position not found: " + symbol));
        
        position.setCurrentPrice(currentPrice);
        position.setLastUpdated(LocalDateTime.now());
        positionRepository.save(position);
        
        recalculatePortfolio(portfolioId);
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceResponse getPortfolioPerformance(Long portfolioId) {
        PortfolioSummary summary = summaryRepository.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio summary not found"));
        return PerformanceResponse.builder()
                .totalReturn(summary.getTotalPnl())
                .totalReturnPercent(summary.getTotalPnlPercent())
                .dayReturn(summary.getDayPnl())
                .dayReturnPercent(summary.getDayPnlPercent())
                .build();
    }

    @Override
    @Transactional
    public void deactivatePortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found: " + portfolioId));
        
        portfolio.setStatus("INACTIVE");
        portfolioRepository.save(portfolio);
        
        log.info("Portfolio deactivated: {}", portfolioId);
    }

    // Helper methods
    
    private String generateAccountNumber() {
        String prefix = "PF";
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return prefix + uniqueId;
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio, PortfolioSummary summary) {
        PortfolioResponse.PortfolioResponseBuilder builder = PortfolioResponse.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUserId())
                .accountNumber(portfolio.getAccountNumber())
                .accountType(portfolio.getAccountType())
                .currency(portfolio.getCurrency())
                .status(portfolio.getStatus())
                .createdAt(portfolio.getCreatedAt());
        
        if (summary != null) {
            builder.totalValue(summary.getTotalValue())
                    .securitiesValue(summary.getSecuritiesValue())
                    .cashBalance(summary.getCashBalance())
                    .investedAmount(summary.getInvestedAmount())
                    .totalPnl(summary.getTotalPnl())
                    .totalPnlPercent(summary.getTotalPnlPercent())
                    .dayPnl(summary.getDayPnl())
                    .dayPnlPercent(summary.getDayPnlPercent())
                    .lastUpdated(summary.getLastUpdated());
        }
        
        return builder.build();
    }

    private PositionResponse mapToPositionResponse(PortfolioPosition position) {
        return PositionResponse.builder()
                .id(position.getId())
                .portfolioId(position.getPortfolio().getId())
                .symbol(position.getSymbol())
                .libelle(position.getLibelle())
                .quantity(position.getQuantity())
                .averageCost(position.getAverageCost())
                .currentPrice(position.getCurrentPrice())
                .marketValue(position.getMarketValue())
                .unrealizedPnl(position.getUnrealizedPnl())
                .performancePercent(position.getPerformancePercent())
                .weightPercent(position.getWeightPercent())
                .lastUpdated(position.getLastUpdated())
                .build();
    }
}
